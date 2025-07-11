package com.poloman.bota

enum class FileType{
    MUSIC,
    PHOTO,
    VIDEO,
    DOC,
    OTHER
}

data class dev_store(
    val fileName : String, val type : Int, val lastModified : String )
