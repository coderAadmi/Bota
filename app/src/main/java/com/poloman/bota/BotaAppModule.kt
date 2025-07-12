package com.poloman.bota

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BotaAppModule {

    @Provides
    @Singleton
    fun provideBotaRepository(@ApplicationContext app : Context, botaAppDb: BotaAppDb) = BotaRepository(appContext = app, botaAppDb)

    @Provides
    @Singleton
    fun provideBotaDb(@ApplicationContext app : Context) : BotaAppDb =
        Room.databaseBuilder(app, BotaAppDb::class.java,"bota_store")
            .fallbackToDestructiveMigration(true).build()
}