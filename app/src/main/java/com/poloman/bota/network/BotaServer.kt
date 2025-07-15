package com.poloman.bota.network

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.poloman.bota.BotaUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.ServerSocket

class BotaServer {

    sealed class ServerState {
        data class Error(val error: Exception) : ServerState()
        object Running : ServerState()
        object Stopped : ServerState()
    }

    lateinit var networkCallback: NetworkService.NetworkCallback

    private lateinit var serverSocket: ServerSocket
    private var port = 0
    private val clients = mutableMapOf<String, BotaUser>()

    private var isActive = false
    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.R)
    constructor(port: Int, networkCallback: NetworkService.NetworkCallback) {
        this.port = port
        this.networkCallback = networkCallback
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun initServer() {
        try {
            serverSocket = ServerSocket(port)
            Log.d("BTU_SERVER", "BTU Server Started")
            Log.d("BTU_SERVER_DETAIL", " address : ${Helper.getdeviceIpAddress()} port : $port")
            isActive = true
            networkCallback.onServerStarted()
            _serverState.value = ServerState.Running
            acceptClients()
        } catch (e: IOException) {
            Log.d("BTU_SERVER_INIT", "Exception ${e.toString()}")
            _serverState.value = ServerState.Error(e)

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun connectToHost(hostAddress: String, port: Int = 3443) {
        try {
            val clientListener = BotaClient(hostAddress, port)
            val clientCommander = BotaClient(hostAddress, port)
            clientListener.startListening(
                onNameAsked = { hostName ->
                    if (clients.containsKey(clientCommander.getIpAddress())) {
                        //inform user already connected
                    } else {
                        clients.put(
                            clientCommander.getIpAddress(),
                            BotaUser(
                                uname = hostName, ip = clientCommander.getIpAddress(),
                                commander = clientCommander,
                                listener = clientListener,
                                onDisconnected = { clientIp ->
                                    Log.d("BTU_CLI_REMOVED", "Client $clientIp disconnected")
                                    clients.remove(clientIp)
                                }
                            ).setCallback(networkCallback)
                        )
                    }
                }
            )

        } catch (e: Exception) {
            Log.d("BTU_CON2HOST", e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun acceptClients() {
        CoroutineScope(Dispatchers.IO).launch {
            var botaClientHost: BotaClient? = null
            var botaClientServer: BotaClient? = null
            while (isActive) {
                try {
                    Log.d("BTU_SERVER", "Waiting for client")
                    val client = serverSocket.accept()
                    botaClientHost = BotaClient(client)
                    //ask for name and validate then connect the listening channel
                    botaClientServer = BotaClient(serverSocket.accept())
                    botaClientHost.sendCommand("UNAME Poloman-Android")
                    val from = botaClientHost.recv() as Result.CommandResponse
                    val uname = from.result.substringAfter("UNAME ")
                    clients.put(
                        botaClientHost.getIpAddress(),
                        BotaUser(
                            uname,
                            botaClientHost.getIpAddress(),
                            botaClientHost,
                            botaClientServer,
                            onDisconnected = {
                                clientIp ->
                                clients.remove(clientIp)
                            }
                        ).setCallback(networkCallback)
                    )
                    networkCallback.onConnectionRequest(uname, botaClientHost.getIpAddress())
                } catch (e: Exception) {
                    Log.d("BTU_SERVER", "Exception ${e.toString()}")
                    botaClientHost?.closeConnection()
//                    botaClientServer?.closeConnection()
                    _serverState.value = ServerState.Error(e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startListeningFromClient(ip: String) {
        clients.get(ip)?.startListening()
    }

    fun stopServer() {
        clients.values.forEach {
            try {
                it.closeConnection()
            } catch (e: IOException) {
                Log.d("BOTA_CL_CLOSE", "Exception ${e.toString()}")
            }
        }

        try {
            serverSocket.close()
        } catch (e: IOException) {
            Log.d("BTU_SERVER_CLOSE", "Exception ${e.toString()}")
        } finally {
            _serverState.value = ServerState.Stopped
        }

    }

    fun denyConnection(ip: String) {
        try {
            clients.get(ip)?.closeConnection()
        } catch (e: Exception) {

        } finally {
            Log.d("BOTA_SERVER", "Connection closed")
        }
    }

    suspend fun sendFile(sendTo: String, file: File) {
        clients.get(sendTo)!!.sendFile(file)
    }

    suspend fun sendDir(sendTo: String, dir: String) {
        clients.get(sendTo)!!.sendDir(dir)
    }

    fun getClients(): List<BotaUser> {
        return clients.values.toList()
    }

    suspend fun receiveFileFrom(ip: String, fname: String, size :Long) {
        clients.get(ip)!!.receiveFile(fname,size)
    }

}