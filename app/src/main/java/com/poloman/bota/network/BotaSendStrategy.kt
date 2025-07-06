package com.poloman.bota.network

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class BotaSendStrategy : SendStrategy {
    override fun sendFile(
        file: File,
        bos: BufferedOutputStream,
        bis: BufferedInputStream
    ): Result {
        val ftSize = 1024
        val map = HashMap<String,String>()
        map.put("name",file.name);
        map.put("size",file.length().toString())
        bos.write("FILE[${file.name.length}] ${file.name}".toByteArray())
        bos.write("SIZE ${file.length()}".toByteArray())
        bos.flush()

        var cmd = String(bis.readBytes())

        if(cmd.equals("SND ${file.name}")){
            try {

                val fis = FileInputStream(file)
                var byteArray = ByteArray(ftSize)
                var readBytes = fis.read(byteArray,0,ftSize)
                var bytesSent : Long= 0

                while (readBytes > 0){
                    bos.write(byteArray,0,readBytes)
                    bytesSent += readBytes
                    Log.d("BTU_BYTES_SENT","Bytes Sent $readBytes  Total : $bytesSent" )
                    readBytes = fis.read(byteArray,0,ftSize)
                }
                bos.flush()
                fis.close()
                Log.d("BTU_FILE_SENT","SENT ${file.name} bytes sent = $bytesSent")

            }
            catch (e : IOException){
                return Result.Error(e)
            }
        }
        return Result.Success
    }

    override fun sendCommand(
        cmd: String,
        bos: BufferedInputStream,
        bis: BufferedInputStream
    ): Result {
        TODO("Not yet implemented")
    }

    override fun sendDir(
        path: String,
        bos: BufferedInputStream,
        bis: BufferedInputStream
    ): Result {
        TODO("Not yet implemented")
    }
}