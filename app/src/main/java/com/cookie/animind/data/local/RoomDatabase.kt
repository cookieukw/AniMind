package com.cookie.animind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Database
import androidx.room.RoomDatabase

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<Setting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: Setting)
}

@Database(entities = [Setting::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao
}
