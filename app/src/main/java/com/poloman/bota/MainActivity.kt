package com.poloman.bota

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.poloman.bota.service.MonitorService
import com.poloman.bota.ui.theme.BotaTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    val requestSettingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result : ActivityResult ->
        if(Environment.isExternalStorageManager())
        {
            Log.d("PER_SET",Environment.getRootDirectory().path)
            monitor()
        }
        else{
            //show user error
            Log.d("PER_SET","Setting not granted")
        }

    }

    val permissionLauncher  = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted : Boolean ->
        if(isGranted){
            Log.d("PER_PER","Permission granted")
            Intent(applicationContext, MonitorService::class.java).also {
                it.action = MonitorService.Action.START_MONITOR.toString()
                startForegroundService(it)
            }
        }
        else{

        }
    }

     var monitorService: MonitorService? = null


    private val connection = object : ServiceConnection{
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            monitorService = (service as MonitorService.MonitorServiceBinder).getService()
            Log.d("PER_BND","Service bound")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            monitorService = null
            Log.d("PER_BND","Service Unbound")
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = {
                            askPermission()
                        }) {
                            Text("Click")
                        }
                    }
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun askPermission(){
        if(!Environment.isExternalStorageManager()){
            requestSettingLauncher.launch(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
        else{
            Log.d("PER_SET",Environment.getRootDirectory().path)
            monitor()
        }
    }

    fun monitor(){
        if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            Intent(applicationContext, MonitorService::class.java).also {
                it.action = MonitorService.Action.START_MONITOR.toString()
                startForegroundService(it)
            }
        }
        else{
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }


        bindService(Intent(this, MonitorService::class.java),connection,BIND_ABOVE_CLIENT)
    }

}
