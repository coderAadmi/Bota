package com.poloman.bota.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.room.Room
import com.poloman.bota.BotaAppDb
import com.poloman.bota.BotaFile
import com.poloman.bota.FileType
import com.poloman.bota.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MonitorService : Service() {

    inner class MonitorServiceBinder : Binder() {
        fun getService(): MonitorService = this@MonitorService
    }

    private val botaAppDb by lazy {
        Room.databaseBuilder(applicationContext, BotaAppDb::class.java,"bota_store").fallbackToDestructiveMigration(true).build()
    }

    enum class Action {
        START_MONITOR,
        STOP_MONITOR
    }

    private val binder = MonitorServiceBinder()
    private lateinit var notificationManager: NotificationManager
    var onFileDiscovered: OnFileDiscovered? = null
    var isActive = true

    override fun onBind(intent: Intent?): IBinder? {
        isActive = true
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BOTA_SER", "On Create called")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initMonitoring()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PER_SER", "On Start called")
        return START_STICKY
    }

    fun initMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            monitor(Environment.getExternalStorageDirectory().path)
        }
    }


    private fun monitor(path: String) {
        if (!isActive) {
            return
        }

//        onFileDiscovered!!.onDirDiscovered(path)

        val file = File(path)
        if (file.isDirectory) {
            file.listFiles()?.let {
                it.forEach { f ->
                    if (f.isDirectory) {
                        monitor(f.path)
                    } else if (f.isFile) {
                        trackFile(f)
//                        Log.d("PER_MON", "${f.name}")
//                        onFileDiscovered!!.onFileDiscovered(f)
                    }
                }
            }
        }
    }

    private fun trackFile(file: File) {
        if (file.exists()) {
            if (file.extension.equals("png", true) ||
                file.extension.equals("jpeg", true) ||
                file.extension.equals("jpg", true) ||
                file.extension.equals("heic", true)
            ){
                CoroutineScope(Dispatchers.IO).launch {
                    botaAppDb.getBotaDao().insert(BotaFile(file.absolutePath,file.name, FileType.Photo.ordinal,file.lastModified()))
                }
            }
            else if(file.extension.equals("mp4", true) ||
                file.extension.equals("mov", true) ||
                file.extension.equals("mkv", true) ||
                file.extension.equals("mpeg", true) ||
                file.extension.equals("webm",true)
                ){
                CoroutineScope(Dispatchers.IO).launch {
                    botaAppDb.getBotaDao().insert(BotaFile(file.absolutePath,file.name, FileType.Videos.ordinal,file.lastModified()))
                }
            }
            else if(file.extension.equals("pdf", true) ||
                file.extension.equals("doc", true) ||
                file.extension.equals("odt", true) ||
                file.extension.equals("rtf", true) ||
                file.extension.equals("csv",true) ||
                file.extension.equals("txt", true)
            ){
                CoroutineScope(Dispatchers.IO).launch {
                    botaAppDb.getBotaDao().insert(BotaFile(file.absolutePath,file.name, FileType.Documents.ordinal,file.lastModified()))
                }
            }
            else if(file.extension.equals("mp3", true) ||
                file.extension.equals("wav", true) ||
                file.extension.equals("wma", true) ||
                file.extension.equals("flac")
                ){

                CoroutineScope(Dispatchers.IO).launch {
                    botaAppDb.getBotaDao().insert(BotaFile(file.absolutePath,file.name, FileType.Music.ordinal,file.lastModified()))
                }
            }
            else{
                CoroutineScope(Dispatchers.IO).launch {
                    botaAppDb.getBotaDao().insert(BotaFile(file.absolutePath,file.name, FileType.Others.ordinal,file.lastModified()))
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

    fun setOnFileDiscoveredCallback(onFileDiscovered: OnFileDiscovered) {
        this.onFileDiscovered = onFileDiscovered
    }
}




