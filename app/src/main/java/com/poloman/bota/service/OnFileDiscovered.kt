package com.poloman.bota.service

import java.io.File

interface OnFileDiscovered {
    fun onFileDiscovered(fileName : String)
    fun onDirDiscovered(dirName : String)
    fun onFileDiscovered(file : File)
}