package com.poloman.bota

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FileType{
    Photo,
    Videos,
    Music,
    Documents,
    Others
}

@Entity(tableName = "dev_Store")
data class BotaFile(
    @PrimaryKey(autoGenerate = false)
    val pathAndName : String,
    val fileName : String,
    val type : Int,
    val lastModified : Long )
