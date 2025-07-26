package com.poloman.bota.network

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.poloman.bota.BotaUser
import com.poloman.bota.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.mutableListOf


@SuppressLint("NewApi")
class NetworkService : Service() {

    interface NetworkCallback {
        fun onConnectionRequest(from: String, ip: String)
        fun onDataIncomingRequest(from: String, ip: String, fileName: String, size: Long)
        fun onMultipleFilesIncomingRequest(from: String, ip: String, fileCount: Int, size: Long)
        fun onServerStarted()
        fun onIncomingProgressChange(ip: String, progress: TransferProgress)
        fun onOutgoingProgressChange(ip: String, progress: TransferProgress)
        fun onStatusChange(ip: String, progress: TransferProgress)
    }

    var serverName = "${android.os.Build.BRAND} ${android.os.Build.MODEL} ${android.os.Build.USER}"

    var networkCallback: NetworkCallback? = null

    inner class NetworkServiceBinder : Binder() {
        fun getService(): NetworkService = this@NetworkService
    }

    private val binder = NetworkServiceBinder()
    private lateinit var notificationManager: NotificationManager

    val botaServer by lazy {
        BotaServer(3443, serverName, networkCallback!!)
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
            val notification: Notification = NotificationCompat.Builder(this, "CHANNEL_ID")
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
        } catch (e: Exception) {
            Log.d("PER_EX", e.toString())
        }
    }

    private fun createServiceNotificationChannel() {
        val channel = NotificationChannel(
            "CHANNEL_ID",
            "Foreground Service channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    fun getConnectedUsers(): List<BotaUser> {
        return botaServer.getClients()
    }


    override fun onDestroy() {
        super.onDestroy()
        botaServer.stopServer()
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

    fun setUserName(name : String) {
        botaServer.updateUserName(name)
    }
}




