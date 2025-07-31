package com.poloman.bota.network

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.poloman.bota.network.BotaUser
import com.poloman.bota.MainActivity
import com.poloman.bota.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.mutableListOf


@SuppressLint("NewApi")
class NetworkService : Service(), NetworkService.NetworkCallback {

    public interface NetworkCallback {
        fun onConnectionRequest(from: String, ip: String)
        fun onDataIncomingRequest(from: String, ip: String, fileName: String, size: Long)
        fun onMultipleFilesIncomingRequest(from: String, ip: String, fileCount: Int, size: Long)
        fun onServerStarted()
        fun onServerStopped()
        fun onIncomingProgressChange(ip: String, progress: TransferProgress)
        fun onOutgoingProgressChange(ip: String, progress: TransferProgress)
        fun onStatusChange(ip: String, progress: TransferProgress)
        fun onClientDisconnected(ip: String, uname : String)
    }

    var serverName = "${Build.BRAND} ${Build.MODEL} ${Build.USER}"

    var networkCallbackFromActivity: NetworkCallback? = null

    inner class NetworkServiceBinder : Binder() {
        fun getService(): NetworkService = this@NetworkService
    }

    private val binder = NetworkServiceBinder()
    private lateinit var notificationManager: NotificationManager
    var isAppInBackground : Boolean = false

    val botaServer by lazy {
        BotaServer(3443, serverName, this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BTU_NET", "On Create called")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BTU_NET", "On Start called")
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun initServer() {
        CoroutineScope(Dispatchers.IO).launch {
            botaServer.initServer()
        }
    }

    fun acceptConnection(ip: String) {
        botaServer.startListeningFromClient(ip)
    }

    fun denyConnection(ip: String) {
        botaServer.denyConnection(ip)
    }

    fun getServerState(): StateFlow<BotaServer.ServerState> {
        return botaServer.serverState
    }

    fun sendFile(sendTo: String, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = GetFile.getFile(this@NetworkService, uri)
            botaServer.sendFile(sendTo, file)
            file.delete()
        }
    }

    fun sendFiles(sendTo: String, uris: List<Uri>) {
        CoroutineScope(Dispatchers.IO).launch {
            val files = mutableListOf<File>()
            uris.forEach { uri ->
                val file = GetFile.getFile(this@NetworkService, uri)
                files.add(file)
            }
            botaServer.sendFile(sendTo, files)
            files.forEach {
                it.delete()
            }

        }
    }


    fun sendDir(sendTo: String, dirName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            botaServer.sendDir(sendTo, dirName)
        }
    }


    private fun startForeground() {
        Log.d("PER_SER_N", "Creating notification")
        try {

            createServiceNotificationChannel()
            createNotification()
        } catch (e: Exception) {
            Log.d("PER_EX", e.toString())
        }
    }

    private fun createNotification() {
        val notification: Notification = NotificationCompat.Builder(this, "Bota-App")
            .setContentTitle("BoTA Network")
            .setContentText("BoTA Server running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the service in the foreground
        ServiceCompat.startForeground(
            this,
            101, notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    fun createTransmissionNotification(clientId: String, cName: String, progress: Int) {

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val title =
                if (clientId.startsWith("FROM")) "Receiving from $cName" else "Sending to $cName"

            val builder = NotificationCompat.Builder(this, "Bota-App")
                .setContentTitle(title)
                .setContentText("Progress: $progress%")
                .setSmallIcon(R.drawable.download)
                .setOngoing(progress < 100)
                .setProgress(100, progress, false)
                .setOnlyAlertOnce(true)

            NotificationManagerCompat.from(this).notify(clientId.hashCode(), builder.build())

            if (progress == 100) {
                NotificationManagerCompat.from(this).cancel(clientId.hashCode())
            }
        }
    }

    fun createPermissionNotification(clientId : String, requestText : String) {

        if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val builder = NotificationCompat.Builder(this, "Bota-App")
                .setContentTitle("Permission Request")
                .setContentText(requestText)
                .setSmallIcon(R.drawable.download)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            NotificationManagerCompat.from(this).notify(clientId.hashCode(), builder.build())

        }
    }

    private fun createServiceNotificationChannel() {
        val channel = NotificationChannel(
            "Bota-App",
            "Bota Network running",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    fun getConnectedUsers(): List<BotaUser> {
        return botaServer.getClients()
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            botaServer.stopServer()
        }
        catch (e : Exception){

        }

        Log.d("BTU_S", "Destroyed")
    }

    fun connectToServer(hostServer: String) {
        CoroutineScope(Dispatchers.IO).launch {
            botaServer.connectToHost(hostServer)
        }
    }

    fun sendMousePos(x: Int, y: Int) {

    }

    fun acceptFile(ip: String, fileName: String, size: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            botaServer.receiveFileFrom(ip, fileName, size)
        }
    }

    fun acceptFile(ip: String, fileCount: Int, size: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            botaServer.receiveFileFrom(ip, fileCount, size)
        }
    }

    fun denyFile(ip: String) {
        CoroutineScope(Dispatchers.IO).launch {
            botaServer.denyFile(ip)
        }
    }

    fun setUserName(name: String) {
        botaServer.updateUserName(name)
    }

    override fun onConnectionRequest(from: String, ip: String) {
        if(isAppInBackground){
            createPermissionNotification(ip,"Connection request from $from")
        }
        networkCallbackFromActivity?.onConnectionRequest(from, ip)
    }

    override fun onDataIncomingRequest(
        from: String,
        ip: String,
        fileName: String,
        size: Long
    ) {
        networkCallbackFromActivity?.onDataIncomingRequest(from, ip, fileName, size)
    }

    override fun onMultipleFilesIncomingRequest(
        from: String,
        ip: String,
        fileCount: Int,
        size: Long
    ) {
        if(isAppInBackground){
            createPermissionNotification(ip,"Incoming files $from")
        }
        networkCallbackFromActivity?.onMultipleFilesIncomingRequest(from, ip, fileCount, size)
    }

    override fun onServerStarted() {
        networkCallbackFromActivity?.onServerStarted()
    }

    override fun onServerStopped() {
        networkCallbackFromActivity?.onServerStopped()
    }

    override fun onIncomingProgressChange(
        ip: String,
        progress: TransferProgress
    ) {
        val tpo = (progress as TransferProgress.Transmitted)
        createTransmissionNotification("FROM $ip", tpo.uname, tpo.progress)
        networkCallbackFromActivity?.onIncomingProgressChange(ip, progress)
    }

    override fun onOutgoingProgressChange(
        ip: String,
        progress: TransferProgress
    ) {
        val tpo = (progress as TransferProgress.Transmitted)
        createTransmissionNotification("TO $ip", tpo.uname, tpo.progress)
        networkCallbackFromActivity?.onOutgoingProgressChange(ip, progress)
    }

    override fun onStatusChange(
        ip: String,
        progress: TransferProgress
    ) {
        networkCallbackFromActivity?.onStatusChange(ip, progress)
    }

    override fun onClientDisconnected(ip :String, uname: String) {
        networkCallbackFromActivity?.onClientDisconnected(ip, uname)
    }

    fun stopServer() {
        botaServer.stopServer()
    }
}




