package com.poloman.bota.service

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
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.poloman.bota.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MonitorService : Service() {

    inner class MonitorServiceBinder : Binder() {
        fun getService(): MonitorService = this@MonitorService
    }

    enum class Action {
        START_MONITOR,
        STOP_MONITOR
    }

    private val binder = MonitorServiceBinder()
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PER_SER", "On Create called")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PER_SER", "On Start called")
//        initMonitoring()
        return START_STICKY
    }

    private fun initMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            monitor(Environment.getExternalStorageDirectory().path)
        }
    }

    private fun monitor(path: String, pathLevel: String = "=") {
        if (path.endsWith(".apk"))
            return
        Log.d("PER_MON", "$pathLevel$path")
        val file = File(path)
        if (file.isDirectory) {
            file.listFiles()?.let {
                it.forEach { f ->
                    if (f.isDirectory) {
                        monitor(f.path, "$pathLevel=")
                    } else if (f.isFile) {
                        Log.d("PER_MON", "${f.name}")
                    }
                }
            }
        }
    }

    private fun startForeground() {


        Log.d("PER_SER_N", "Creating notification")
        try {

            createServiceNotificationChannel()
            val notification: Notification = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("Foreground Service")
                .setContentText("Foreground Service demonstration")
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
}




