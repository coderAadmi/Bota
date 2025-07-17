package com.poloman.bota

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.poloman.bota.network.BotaClient
import com.poloman.bota.network.NetworkService
import java.io.File

class BotaUser {
    lateinit var commander : BotaClient
    lateinit var listener : BotaClient
    lateinit var uname: String
    lateinit var ip: String
    lateinit var networkCallback: NetworkService.NetworkCallback


    interface BotaClientCallback{
        fun onFileIncomingRequest(filename : String, size : Long)
        fun onMultipleFileIncomingRequest(fileCount : Int, size: Long)
    }

    constructor(uname : String, ip : String, commander : BotaClient, listener : BotaClient, onDisconnected : (ip : String) -> Unit){
        this.commander = commander
        this.listener = listener
        this.uname = uname
        this.ip = ip
        this.commander.onDisconnect = {
            onDisconnected(this.ip)
        }
        this.listener.onDisconnect = {
            onDisconnected(this.ip)
        }
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
            Log.d("BTU_CLOSE_CLIENT",e.toString())
        }
    }

    suspend fun sendFile(file: File) {
            commander.askPermissionToSendFile(file)
    }

    suspend fun sendFile(files: List<File>) {
        commander.askPermissionToSendFiles(files)
    }

    suspend fun sendDir(dir : String ){
            commander.sendDir(dir)
            Log.d("BTU_RV",commander.recv().toString())
    }

    fun  setCallback(callback: NetworkService.NetworkCallback) : BotaUser {
        networkCallback = callback

        listener.setCallback(object : BotaClientCallback{
            override fun onFileIncomingRequest(filename: String, size: Long) {
                networkCallback.onDataIncomingRequest(uname,ip,filename,size)
            }

            override fun onMultipleFileIncomingRequest(fileCount: Int, size: Long) {
                networkCallback.onMultipleFilesIncomingRequest(uname, ip, fileCount, size)
            }

        })
        return this
    }

    fun receiveFile(fileName : String, size : Long){
        listener.initFileReceiver(fileName, size)
    }

    fun receiveFile(fcount : Int, size : Long){
        listener.initFileReceiver(fcount, size)
    }

    fun denyFile(ip : String){
        listener.denyFile(ip)
    }
}