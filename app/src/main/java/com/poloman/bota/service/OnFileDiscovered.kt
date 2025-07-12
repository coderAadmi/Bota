package com.poloman.bota.service

import java.io.File

interface OnFileDiscovered {
    fun onDirDiscovered(dirName : String)
    fun onFileDiscovered(file : File)
}