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
import android.widget.Toast
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
import com.poloman.bota.network.TransferProgress
import com.poloman.bota.screen.AppNavHost
import com.poloman.bota.screen.Destination
import com.poloman.bota.views.PermissionDialog
import com.poloman.bota.service.MonitorService
import com.poloman.bota.service.OnFileDiscovered
import com.poloman.bota.ui.theme.BotaTheme
import com.poloman.bota.views.BotaAppBar
import com.poloman.bota.views.ConnectedUsersDialog
import com.poloman.bota.views.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    val requestSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (Environment.isExternalStorageManager()) {
                networkService?.initServer()
            } else {
                //show user error
                Log.d("PER_SET", "Setting not granted")
                Toast.makeText(this,"Can not perform data transfer without storage access",
                    Toast.LENGTH_SHORT).show()
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
//                        vm.setNetworkServiceState(NetworkResponse.ConnectionRequest(from, ip))
                        vm.setNetworkReqState(NetworkResponse.ConnectionRequest(from, ip))
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
                        vm.setNetworkReqState(
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

                    override fun onIncomingProgressChange(ip: String, progress: TransferProgress) {
                        Log.d("BOTA_IN_PROGRESS","From $ip progress $progress %")
                        vm.setProgressState("FROM $ip", progress)
                    }

                    override fun onOutgoingProgressChange(ip: String, progress: TransferProgress) {
                        Log.d("BOTA_OUT_PROGRESS","To $ip progress $progress %")
                        vm.setProgressState("TO $ip", progress)
                    }

                    override fun onStatusChange(
                        ip: String,
                        progress: TransferProgress
                    ) {
                        when(progress){
                            is TransferProgress.CalculatingSize -> {
                                vm.showProgressDialog()
                                vm.setProgressState("TO $ip", progress)
                            }
                            is TransferProgress.RequestDenied -> {
                                vm.setProgressState("TO $ip", progress)
                            }
                            is TransferProgress.Success -> {
                                if(progress.isReceiving){
                                    vm.setProgressState("FROM $ip", progress)
                                }
                                else{
                                    vm.setProgressState("TO $ip", progress)
                                }
                            }
                            is TransferProgress.Transmitted -> {

                            }
                            is TransferProgress.WaitingForPermissionToSend -> {
                                vm.setProgressState("TO $ip", progress)
                            }
                            is TransferProgress.WaitingForSender -> {
                                vm.setProgressState("FROM $ip", progress)
                            }
                        }
                    }
                }
            }

            monitorService?.setOnFileDiscoveredCallback(object : OnFileDiscovered {
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

        override fun onServiceDisconnected(name: ComponentName?) {
            networkService = null
            Log.d("BTU_BND", "Network Service Unbound")
        }

    }

    val connector = object : Communicator {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onStartServer() {
            checkStoragePermissions()
        }

        override fun onConnectToServer(ip: String) {
            networkService?.connectToServer(ip)

        }

        override fun showConnectedDevices() {
            if(vm.getSelectedFiles().value.isEmpty()){
                Toast.makeText(this@MainActivity,"Select some files to transfer",
                    Toast.LENGTH_SHORT).show()
            }
            else {
                vm.showUserSelector()
            }
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
                                        if(selectedUsers.isEmpty()) {
                                            Toast.makeText(this@MainActivity,"Please select a user to send the files",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            vm.hideUserSelector()
                                            vm.showProgressDialog()
                                            selectedUsers.forEach { user ->
                                                Log.d(
                                                    "BOTA_USER_LIST",
                                                    "${user.uname} : ${user.ip}"
                                                )
                                                vm.setProgressState(
                                                    "TO ${user.ip}",
                                                    TransferProgress.CalculatingSize(user.uname)
                                                )
                                                networkService?.sendFiles(
                                                    user.ip,
                                                    vm.getSelectedFiles().value
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            false -> {
                            }
                        }
                        PermissionDialog(
                            modifier = Modifier,
                            vm.getPermissionDialogState(),
                            vm.getNetworkReqState(),
                            onAccept = { ip: String, req : NetworkResponse ->
                                networkService?.acceptConnection(ip)
//                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                                vm.removeNetworkReqState(req)
                            },
                            onDeny = { ip: String, req : NetworkResponse ->
                                networkService?.denyConnection(ip)
//                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                                vm.removeNetworkReqState(req)
                            },

                            onFileAccept = { ip: String, fname: String, size: Long, req : NetworkResponse ->
                                networkService?.acceptFile(ip, fileName = fname, size = size)
//                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                                vm.removeNetworkReqState(req)
                            },

                            onMulFilesAccept = { ip: String, fcount: Int, size: Long, req : NetworkResponse ->
                                networkService?.acceptFile(ip, fcount, size)
//                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                                vm.removeNetworkReqState(req)
                            },

                            onFileDeny = { ip: String, req : NetworkResponse ->
                                networkService?.denyFile(ip)
//                                vm.setNetworkServiceState(NetworkResponse.Nothing)
                                vm.removeNetworkReqState(req)
                            }

                        ){
                            // on Dismiss
                            vm.hidePermissionDialog()
                        }

                        ProgressDialog(vm.getProgressDialogState(),vm.getProgressState()){
                            vm.hideProgressDialog()
                        }

                        BotaAppBar(selectedDestination, vm.getProgressState(), vm.getProgressDialogState(),
                            vm.getPermissionDialogState(), vm.getNetworkReqState(),
                            { vm.showProgressDialog() }, { vm.showPermissionDialog()}
                        )


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

//        startMonitorService()
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

    fun checkStoragePermissions(){
        if (!Environment.isExternalStorageManager()) {
            Toast.makeText(this,"Storage access is required to write files to memory",
                Toast.LENGTH_SHORT).show()
            requestSettingLauncher.launch(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else {
            Log.d("PER_SET", Environment.getRootDirectory().path)
            networkService?.initServer()
        }
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
