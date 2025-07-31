package com.poloman.bota

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.poloman.bota.network.BotaClient
import com.poloman.bota.network.BotaClientCallback
import com.poloman.bota.network.NetworkService
import com.poloman.bota.network.TransferProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BotaUser {
    lateinit var commander : BotaClient
    lateinit var listener : BotaClient
    lateinit var uname: String
    lateinit var ip: String
    lateinit var networkCallback: NetworkService.NetworkCallback
    lateinit var connectionAcceptedFromServer : (BotaUser) -> Unit

    val clientCallback = object : BotaClientCallback{
        override fun onFileIncomingRequest(filename: String, size: Long) {
            networkCallback.onDataIncomingRequest(uname,ip,filename,size)
        }

        override fun onMultipleFileIncomingRequest(fileCount: Int, size: Long) {
            networkCallback.onMultipleFilesIncomingRequest(uname, ip, fileCount, size)
        }

        override fun onIncomingProgressChange(progress: Int) {
            networkCallback.onIncomingProgressChange(ip, TransferProgress.Transmitted(uname,progress))
        }

        override fun onOutgoingProgressChange(progress: Int) {
            networkCallback.onOutgoingProgressChange(ip, TransferProgress.Transmitted(uname,progress))
        }

        override fun onConnectionAccepted() {
            connectionAcceptedFromServer(this@BotaUser)
        }

        override fun onWaitingForPermissionToSend() {
            networkCallback.onStatusChange(ip, TransferProgress.WaitingForPermissionToSend(uname))
        }

        override fun onAcceptRequest() { // self accepts incoming request
            networkCallback.onStatusChange(ip, TransferProgress.WaitingForSender(uname))
        }

        override fun onRequestAccepted() { //client accepted the request
            networkCallback.onStatusChange(ip, TransferProgress.CalculatingSize(uname))
        }

        override fun onRequestDenied() {
            networkCallback.onStatusChange(ip, TransferProgress.RequestDenied(uname))
        }

        override fun onAllFilesSent() {
            networkCallback.onStatusChange(ip, TransferProgress.Success(uname, false))
        }

        override fun onAllFilesReceived() {
            networkCallback.onStatusChange(ip, TransferProgress.Success(uname, true))
        }

        override fun onClientDisconnected() {
            this@BotaUser.commander.onDisconnect()
        }

    }


    constructor(uname : String, ip : String, commander : BotaClient, listener : BotaClient, onDisconnected : (ip : String) -> Unit){
        this.commander = commander
        this.listener = listener
        this.uname = uname
        this.ip = ip
        this.commander.onDisconnect = {
            networkCallback?.onClientDisconnected(ip,uname)
            onDisconnected(this.ip)
        }
        this.listener.onDisconnect = {
            networkCallback?.onClientDisconnected(ip, uname)
            onDisconnected(this.ip)
        }
    }

    fun setConnectionAcceptedLambda(lambda : (BotaUser) -> Unit){
        connectionAcceptedFromServer = lambda
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startListening(){
        CoroutineScope(Dispatchers.IO).launch {
            commander.sendCommand("ACCEPTED_CONNECTION")
        }
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

        listener.setCallback(clientCallback)
        commander.setCallback(clientCallback)
        return this
    }

    fun receiveFile(fileName : String, size : Long){
        listener.initFileReceiver(fileName, size)
    }

    fun receiveFile(fcount : Int, size : Long){
        listener.initFileReceiver(fcount, size)
        clientCallback.onAcceptRequest()
    }

    fun denyFile(){
        listener.denyFile()
    }

    fun updateUserName(newName: String) {
        commander.updateName(newName)
        listener.updateName(newName)

    }
}