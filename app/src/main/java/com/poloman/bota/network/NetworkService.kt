package com.poloman.bota.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.poloman.bota.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class NetworkService : Service() {

    inner class NetworkServiceBinder : Binder() {
        fun getService(): NetworkService = this@NetworkService
    }

    private val binder = NetworkServiceBinder()
    private lateinit var notificationManager: NotificationManager

    lateinit var botaServer: BotaServer

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
            botaServer =  BotaServer(3443)
        }
    }

    fun sendFile(file : File){
        Log.d("BTU_SEND_FILE","Sending ${file.name}")
        botaServer.botaClientHost.sendFile(file)
    }

    fun sendDir(dirName : String){
        botaServer.botaClientHost.sendDir(dirName)
        Log.d("BTU_RV",botaServer.botaClientHost.recv().toString())
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

    override fun onDestroy() {
        super.onDestroy()
        botaServer.stopServer()
        Log.d("BTU_S","Destroyed")
    }
}




