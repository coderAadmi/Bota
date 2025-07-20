package com.poloman.bota.network

sealed class TransferProgress {
    data class WaitingForSender(val uname : String) : TransferProgress()
    data class CalculatingSize(val uname : String) : TransferProgress()
    data class RequestDenied(val uname : String) : TransferProgress()
    data class WaitingForPermissionToSend(val uname : String) : TransferProgress()
    data class Success(val uname : String, val isReceiving : Boolean) : TransferProgress()
    data class Transmitted(val uname: String, val progress : Int) : TransferProgress()
}