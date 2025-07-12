package com.poloman.bota

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.poloman.bota.network.BotaClient
import com.poloman.bota.network.NetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.Socket

class BotaUser {
    lateinit var commander : BotaClient
    lateinit var listener : BotaClient
    lateinit var uname: String
    lateinit var ip: String
    lateinit var permissionCallback: NetworkService.PermissionCallback

    interface BotaClientCallback{
        fun onFileIncomingRequest(filename : String, size : Long)
    }

    constructor(uname : String, ip : String, commander : BotaClient, listener : BotaClient){
     this.commander = commander
        this.listener = listener
        this.uname = uname
        this.ip = ip
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startListening(){
        listener.startListening()
    }

    fun closeConnection() {
        try {
            listener.closeConnection()
            commander.closeConnection()
        }
        catch (e : Exception){

        }
    }

    suspend fun sendFile(file: File) {
            commander.sendFile(file)
    }

    suspend fun sendDir(dir : String ){
            commander.sendDir(dir)
            Log.d("BTU_RV",commander.recv().toString())
    }

    fun setCallback(callback: NetworkService.PermissionCallback) : BotaUser {
        permissionCallback = callback

        listener.setCallback(object : BotaClientCallback{
            override fun onFileIncomingRequest(filename: String, size: Long) {
                permissionCallback.onDataIncomingRequest(uname,ip,filename,size)
            }

        })
        return this
    }

    fun receiveFile(fileName : String, size : Long){
        listener.initFileReceiver(fileName, size)
    }
}