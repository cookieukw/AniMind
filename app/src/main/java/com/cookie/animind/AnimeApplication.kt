package com.cookie.animind

import android.app.Application
import androidx.room.Room
import com.cookie.animind.data.local.AppDatabase
import com.cookie.animind.repository.AnimeRepository
import com.cookie.animind.repository.SettingsRepository

class AnimeApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set

        lateinit var animeRepository: AnimeRepository
            private set

        lateinit var settingsRepository: SettingsRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "anime_db").build()
        animeRepository = AnimeRepository()
        settingsRepository = SettingsRepository(database.settingDao())
    }
}
