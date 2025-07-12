package com.poloman.bota

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BotaFile::class], version = 2)
abstract class BotaAppDb : RoomDatabase() {

    abstract fun getBotaDao() : BotaDao
}