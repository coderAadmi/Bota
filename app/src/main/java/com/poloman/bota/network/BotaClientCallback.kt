package com.poloman.bota.network

interface BotaClientCallback{
        fun onFileIncomingRequest(filename : String, size : Long)
        fun onMultipleFileIncomingRequest(fileCount : Int, size: Long)
        fun onIncomingProgressChange(progress : Int)
        fun onOutgoingProgressChange(progress : Int)
        fun onConnectionAccepted()
        fun onWaitingForPermissionToSend()
        fun onAcceptRequest() // incoming files accepted
        fun onRequestAccepted() // client accepted files request 
        fun onRequestDenied() // client denied the request
        fun onAllFilesSent()
        fun onAllFilesReceived()
        fun onClientDisconnected()
    }