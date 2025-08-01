package com.poloman.bota.network

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.poloman.bota.network.BotaUser
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketException
import java.nio.ByteBuffer

class BotaTransferStrategy : SendStrategy {

    private var incomingFileSize = 0L
    private var outGoingFileSize = 0L
    private var totalSentSize = 0L
    private var totalReadSize = 0L
    private lateinit var clientCallback: BotaClientCallback
    private var lastSendProgress = 0
    private var lastReceiveProgress = 0

    @RequiresApi(Build.VERSION_CODES.R)
    val root = "${Environment.getExternalStorageDirectory().path}${File.separator}BotaStorage${File.separator}"

    fun setFileIncomingSize(incomSize : Long){
        incomingFileSize = incomSize
        totalReadSize = 0L
        lastReceiveProgress = 0
    }

    fun setCallback(callback : BotaClientCallback){
        clientCallback = callback
    }

    override fun sendFile(
        file: File,
        bos: BufferedOutputStream,
        bis: BufferedInputStream
    ): Result {
        val ftSize = 2048
        val filePath = "${file.name}"
        try
        {
        sendCommand("RCV_FILE $filePath",bos,bis)
        var reply = recvCommand(bos,bis) as Result.CommandResponse
        if(reply.result.equals("SND_SIZE")){
            sendCommand(file.length().toString(),bos,bis)
        }
        reply = recvCommand(bos,bis) as Result.CommandResponse


        if(reply.result.equals("SND_FILE $filePath")){

                val fis = FileInputStream(file)
                var byteArray = ByteArray(ftSize)
                var readBytes = fis.read(byteArray,0,ftSize)
                var bytesSent : Long= 0

                while (readBytes > 0){
                    bos.write(byteArray,0,readBytes)
                    bytesSent += readBytes
//                    Log.d("BTU_BYTES_SENT","Bytes Sent $readBytes  Total : $bytesSent" )
                    totalSentSize += readBytes
                    val newProgress = (100*(totalSentSize.toFloat() / outGoingFileSize)).toInt()
                    if(newProgress > lastSendProgress){
                        clientCallback.onOutgoingProgressChange(newProgress)
                        lastSendProgress = newProgress
                    }
                    readBytes = fis.read(byteArray,0,ftSize)
                }
                bos.flush()
                fis.close()
                Log.d("BTU_FILE_SENT","SENT ${file.name} bytes sent = $bytesSent")

            }
        }
        catch (e : Exception){
            // clear the bos, send something so that receiver reads -1 bytes
            Log.d("BTU_IO_ERROR",e.toString())
            clientCallback.onClientDisconnected()
            return Result.Error(e)
        }
        return Result.Success
    }

    override fun sendCommand(
        cmd: String,
        bos: BufferedOutputStream,
        bis: BufferedInputStream
    ): Result {
        try {
            val cmdLen = cmd.toByteArray().size
            val byteBuf = ByteBuffer.allocate(Int.SIZE_BYTES)
            byteBuf.putInt(cmdLen)
            bos.write(byteBuf.array())
            bos.write(cmd.toByteArray())
            bos.flush()
            Log.d("BTU_CMD","SENT : $cmd")
            return Result.Success
        } catch (e: SocketException) {
            clientCallback.onClientDisconnected()
            return Result.Error(e)
        }
    }

    override fun sendDir(
        path: String,
        bos: BufferedOutputStream,
        bis: BufferedInputStream
    ): Result {
        return Result.Success
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun recvFile(
        fileName: String,
        size: Long,
        bos: BufferedOutputStream,
        bis: BufferedInputStream
    ): Result {

        var totalBytesRead = 0L
        try {
            val file = File("$root$fileName")
            try {
                file.parentFile.mkdirs()
            }
            catch (e: Exception) {

            }
            file.createNewFile()
            val fos = FileOutputStream(file)

            var byteArray = ByteArray(2048)
            var readBytes = 0
            while (totalBytesRead != size) {
                readBytes = bis.read(byteArray,0,Math.min(2048,size - totalBytesRead).toInt())
                if (readBytes == -1)
                    break
                fos.write(byteArray, 0, readBytes)
                totalBytesRead += readBytes
                totalReadSize += readBytes
                val newProgress = (100 * (totalReadSize.toFloat() / incomingFileSize)).toInt()
                if(newProgress > lastReceiveProgress){
                    clientCallback.onIncomingProgressChange( newProgress )
                    lastReceiveProgress = newProgress
                }
            }
            fos.flush()
            fos.close()
            if (totalBytesRead == size){
                Log.d("BTU_FILE_RECVD","RECVD $fileName")
                if(totalReadSize == incomingFileSize){
                    clientCallback.onAllFilesReceived()
                }
                return Result.Success
            }
        } catch (e: Exception) {
            Log.d("BTU_RCV_FILE", e.toString())
            return Result.Error(e)
        }
        Log.d("BTU_RCV_FILE_ERROR", "File size $size received $totalBytesRead")
        return Result.Error(Exception("File size $size received $totalBytesRead"))
    }

    override fun recvCommand(
        bos : BufferedOutputStream,
        bis: BufferedInputStream
    ): Result {

        try {
            val lenBuf = ByteArray(4)
            bis.readFully(lenBuf) // read 4 bytes completely
            val cmdLen = ByteBuffer.wrap(lenBuf).int

            val MAX_CMD_LEN = 8192
            if (cmdLen <= 0 || cmdLen > MAX_CMD_LEN) {
                throw IOException("Invalid command length: $cmdLen")
            }

            val cmdBytes = ByteArray(cmdLen)
            bis.readFully(cmdBytes)

            val cmd = String(cmdBytes)
            return Result.CommandResponse(cmd)
        } catch (e: SocketException) {
            clientCallback.onClientDisconnected()
            return Result.Error(e)
        }
    }

    fun BufferedInputStream.readFully(buf: ByteArray, offset: Int = 0, length: Int = buf.size) {
        var total = 0
        while (total < length) {
            val read = this.read(buf, offset + total, length - total)
            if (read == -1) throw IOException("Stream closed prematurely")
            total += read
        }
    }

    fun askPermissionToSendFile(file : File, bos : BufferedOutputStream, bis : BufferedInputStream) {
        sendCommand("FILE_INCOMING_PERMISSION ${file.name}", bos ,bis)
        sendCommand("FILE_SIZE ${file.length()}",bos, bis)
        val reply = recvCommand(bos,bis) as Result.CommandResponse

        if(reply.result.equals("OK ${file.name}")){
            sendFile(file, bos, bis)
        }
        else{
            //denied
        }
    }

    fun askPermissionToSendFiles(files : List<File>, bos : BufferedOutputStream, bis : BufferedInputStream) {
        var totalFilesSize = 0L
        files.forEach {
            totalFilesSize += it.length()
        }
        sendCommand("MULTIPLE_FILE_INCOMING_PERMISSION ${files.size}", bos ,bis)
        sendCommand("FILE_SIZE ${totalFilesSize}",bos, bis)
        clientCallback.onWaitingForPermissionToSend()
        val tempRep = recvCommand(bos,bis)
        if(tempRep is Result.Error){
            return
        }
        val reply = tempRep as Result.CommandResponse

        if(reply.result.equals("OK $totalFilesSize")){
            clientCallback.onRequestAccepted()
            outGoingFileSize = totalFilesSize
            totalSentSize = 0L
            lastSendProgress = 0
            files.forEach {
                sendFile(it, bos, bis)
            }
            if(totalSentSize == outGoingFileSize){
                clientCallback.onAllFilesSent()
            }
        }
        else{
            //denied
            clientCallback.onRequestDenied()
        }
    }
}