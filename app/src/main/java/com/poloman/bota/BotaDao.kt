package com.poloman.bota

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BotaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(botaFile: BotaFile)

    @Query("Select * from dev_Store where type is :fileType ORDER BY lastModified DESC")
    fun getFilesByType(fileType: Int) : Flow<List<BotaFile>>

    @Query("Select count(*) from dev_Store where type is :fileType")
    fun getFileTypeCount(fileType: Int) : Flow<Int>

    @Query("Select * from dev_Store where type is :fileType ORDER BY lastModified DESC")
    fun getPagedFilesByCount(fileType: Int) : PagingSource<Int, BotaFile>
}