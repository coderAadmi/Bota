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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.poloman.bota.network.Communicator
import com.poloman.bota.network.NetworkResponse
import com.poloman.bota.network.NetworkService
import com.poloman.bota.screen.AppNavHost
import com.poloman.bota.screen.Destination
import com.poloman.bota.screen.PermissionDialog
import com.poloman.bota.service.MonitorService
import com.poloman.bota.service.OnFileDiscovered
import com.poloman.bota.ui.theme.BotaTheme
import com.poloman.bota.views.BotaAppBar
import com.poloman.bota.views.ConnectedUsersDialog
import com.poloman.bota.views.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
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
            Log.d("PER_BND", "Monitor Service bound")


        }

        override fun onServiceDisconnected(name: ComponentName?) {
            monitorService = null
            Log.d("PER_BND", "Monitor Service Unbound")
        }

    }

    private val netConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            networkService = (service as NetworkService.NetworkServiceBinder).getService()
            Log.d("BTU_BND", "Net Service bound")

            networkService?.let { service ->
                service.networkCallback = object : NetworkService.NetworkCallback {
                    override fun onConnectionRequest(from: String, ip: String) {
                        vm.setNetworkServiceState(NetworkResponse.ConnectionRequest(from, ip))
                    }

                    override fun onDataIncomingRequest(
                        from: String,
                        ip: String,
                        fName: String,
                        size: Long
                    ) {
                        vm.setNetworkServiceState(
                            NetworkResponse.IncomingDataRequest(
                                from,
                                ip,
                                fName,
                                size
                            )
                        )
                    }

                    override fun onMultipleFilesIncomingRequest(
                        from: String,
                        ip: String,
                        fileCount: Int,
                        size: Long
                    ) {
                        vm.setNetworkServiceState(
                            NetworkResponse.IncomingMulDataRequest(
                                from,
                                ip,
                                fileCount,
                                size
                            )
                        )
                    }

                    override fun onServerStarted() {
                        vm.generateQrCode()
                    }

                    override fun onIncomingProgressChange(ip: String, progress: Int) {
                        Log.d("BOTA_IN_PROGRESS","From $ip progress $progress %")
                        vm.setProgressState(ip, progress)
                    }

                    override fun onOutgoingProgressChange(ip: String, progress: Int) {
                        Log.d("BOTA_OUT_PROGRESS","To $ip progress $progress %")
                        vm.setProgressState(ip, progress)
                    }
                }
            }

            monitorService?.let { service ->
                service.setOnFileDiscoveredCallback(object : OnFileDiscovered {
                    override fun onDirDiscovered(dirName: String) {
//                        networkService?.let { it.sendDir(dirName) }
                    }

                    override fun onFileDiscovered(file: File) {
                        if (file.exists()) {
//                            networkService?.let { it.sendFile(file) }
                        }
                    }
                })
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            networkService = null
            Log.d("BTU_BND", "Network Service Unbound")
        }

    }

    val connector = object : Communicator {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onStartServer() {
            networkService?.let {
                it.initServer()
            }
        }

        override fun onConnectToServer(ip: String) {
            networkService?.let {
                it.connectToServer(ip)
            }

        }

        override fun showConnectedDevices() {
            vm.showUserSelector()
        }

    }

    val vm by viewModels<QrViewModel>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotaTheme {
                val navController = rememberNavController()
                val startDestination = Destination.HOME
                var selectedDestination by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    bottomBar = {
                        NavigationBar(
                            windowInsets = NavigationBarDefaults.windowInsets,
                            containerColor = Color(0xFFF7FAFC)
                        ) {
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
                                            contentDescription = ""
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }

                    }
                ) { innerPadding ->

                    Column(modifier = Modifier.padding(innerPadding))
                    {
                        when (vm.userSelectorState.collectAsState().value) {
                            true -> {
                                networkService?.let {
                                    ConnectedUsersDialog(it.getConnectedUsers(), onDismiss = {
                                        vm.hideUserSelector()
                                    })
                                    { selectedUsers ->
                                        selectedUsers.forEach { user ->
                                            Log.d("BOTA_USER_LIST", "${user.uname} : ${user.ip}")
                                                networkService?.sendFiles(
                                                    user.ip,
                                                    vm.getSelectedFiles().value
                                                )
                                        }
                                        vm.hideUserSelector()
                                    }
                                }
                            }

                            false -> {
                            }
                        }
                        PermissionDialog(
                            modifier = Modifier,
                            vm.getNetworkResponseState(),
                            onAccept = { ip: String ->
                                networkService?.acceptConnection(ip)
                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                            },
                            onDeny = { ip: String ->
                                networkService?.denyConnection(ip)
                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                            },

                            onFileAccept = { ip: String, fname: String, size: Long ->
                                networkService?.acceptFile(ip, fileName = fname, size = size)
                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                            },

                            onMulFilesAccept = { ip: String, fcount: Int, size: Long ->
                                networkService?.acceptFile(ip, fcount, size)
                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                            },

                            onFileDeny = { ip: String ->
                                networkService?.denyFile(ip)
                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                            }

                        )

                        ProgressDialog(vm.getProgressDialogState(),vm.getProgressState()){
                            vm.hideProgressDialog()
                        }

                        BotaAppBar(selectedDestination, vm.getProgressState(), vm.getProgressDialogState()){
                            vm.showProgressDialog()
                        }


                        AppNavHost(
                            navController,
                            startDestination,
                            Modifier.padding(innerPadding),
                            communicator = connector
                        )
                    }
                }
            }
        }

        startMonitorService()
        startNetService()

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

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (netConnection != null) {
                unbindService(netConnection)
            }
            if (connection != null) {
                unbindService(connection)
            }

            monitorService?.let { it.stopSelf() }
        } catch (e: Exception) {

        }
    }
}
