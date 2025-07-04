package com.poloman.bota

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
            //show message to user to grant permission
            requestSettingLauncher.launch(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
        else{
            Log.d("PER_SET",Environment.getRootDirectory().path)
        }
    }

    fun monitor(){

    }

}
