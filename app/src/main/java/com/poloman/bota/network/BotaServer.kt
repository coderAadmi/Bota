package com.poloman.bota.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class BotaServer {
    private lateinit var serverSocket : ServerSocket
    private var port = 0
    private val clients = mutableMapOf<String, Socket>()
    lateinit var botaClient: BotaClient

    private var isActive  = false

    constructor(port : Int){
        this.port = port
        initServer()
    }

    private fun initServer() {
        try {
            serverSocket = ServerSocket(port)
            Log.d("BTU_SERVER","BTU Server Started")
            Log.d("BTU_SERVER_DETAIL"," address : ${Helper.getdeviceIpAddress()} port : $port")
            isActive = true
            acceptClients()
        }catch (e : IOException){
            Log.d("BTU_SERVER_INIT","Exception ${e.toString()}")
        }
    }

    private fun acceptClients() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive){
                try {
                    Log.d("BTU_SERVER","Waiting for client")
                    val client = serverSocket.accept()
//                    clients.put("client",client)
                    botaClient = BotaClient(client)
                    Log.d("BTU_SERVER","connected to client : ${client.inetAddress}")
                    isActive = false
                }
                catch (e : Exception){
                    Log.d("BTU_SERVER","Exception ${e.toString()}")
                }
            }
        }
    }

    fun stopServer(){
        try {
            isActive = false
            botaClient.closeConnection()
            serverSocket.close()
        }
        catch (e : IOException){
            Log.d("BTU_SERVER_CLOSE","Exception ${e.toString()}")
        }
    }

}