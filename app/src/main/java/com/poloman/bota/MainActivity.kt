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
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.poloman.bota.network.NetworkService
import com.poloman.bota.screen.AppNavHost
import com.poloman.bota.screen.Destination
import com.poloman.bota.service.MonitorService
import com.poloman.bota.service.OnFileDiscovered
import com.poloman.bota.ui.theme.BotaTheme
import com.poloman.bota.views.HomePage
import java.io.File

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    val requestSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (Environment.isExternalStorageManager()) {
                Log.d("PER_SET", Environment.getRootDirectory().path)
                monitor()
            } else {
                //show user error
                Log.d("PER_SET", "Setting not granted")
            }

        }

    val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PER_PER", "Permission granted")
                Intent(applicationContext, MonitorService::class.java).also {
                    it.action = MonitorService.Action.START_MONITOR.toString()
                    startForegroundService(it)
                }
            } else {

            }
        }

    var monitorService: MonitorService? = null
    var networkService: NetworkService? = null


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            monitorService = (service as MonitorService.MonitorServiceBinder).getService()
            Log.d("PER_BND", "Service bound")
            startNetService()

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            monitorService = null
            Log.d("PER_BND", "Service Unbound")
        }

    }

    private val netConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            networkService = (service as NetworkService.NetworkServiceBinder).getService()
            Log.d("BTU_BND", "Service bound")

            monitorService!!.setOnFileDiscoveredCallback(object : OnFileDiscovered {
                override fun onDirDiscovered(dirName: String) {
                    networkService?.let { it.sendDir(dirName) }
                }

                override fun onFileDiscovered(file: File) {
                    if (file.exists()) {
                        networkService?.let { it.sendFile(file) }
                    }
                }
            })

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            networkService = null
            Log.d("BTU_BND", "Service Unbound")
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotaTheme {
                val navController = rememberNavController()
                val startDestination = Destination.HOME
                var selectedDestination = 0

                Scaffold(modifier = Modifier
                    .fillMaxSize().background(Color.Gray),
                    bottomBar = {
                        NavigationBar(windowInsets = NavigationBarDefaults.windowInsets,
                            containerColor = Color(0xFFF7FAFC)) {
                            Destination.entries.forEachIndexed { index, destination ->
                                NavigationBarItem(
                                    selected = selectedDestination == index,
                                    onClick = {
                                        navController.navigate(route = destination.route)
                                        selectedDestination = index
                                    },
                                    icon = {
                                        Icon(
                                            destination.icon,
                                            contentDescription = destination.contentDescription
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }

                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        AppNavHost(navController,startDestination, Modifier.padding(innerPadding)) }
                }
            }
        }

        startMonitorService()

    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startMonitorService() {
        if (!Environment.isExternalStorageManager()) {
            requestSettingLauncher.launch(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else {
            Log.d("PER_SET", Environment.getRootDirectory().path)
            monitor()
        }
    }

    fun monitor() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Intent(applicationContext, MonitorService::class.java).also {
                it.action = MonitorService.Action.START_MONITOR.toString()
                startForegroundService(it)
            }
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }


        bindService(Intent(this, MonitorService::class.java), connection, BIND_ABOVE_CLIENT)
    }

    fun startNetService() {
        Intent(applicationContext, NetworkService::class.java).also {
            it.action = MonitorService.Action.START_MONITOR.toString()
            startForegroundService(it)
        }
        bindService(Intent(this, NetworkService::class.java), netConnection, BIND_ABOVE_CLIENT)
    }
}
