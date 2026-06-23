package com.cookie.animind.repository

import com.cookie.animind.data.local.Setting
import com.cookie.animind.data.local.SettingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dao: SettingDao) {
    val apiKey: Flow<String> = dao.getSetting("api_key").map { it?.value ?: "" }
    val responseMode: Flow<String> = dao.getSetting("response_mode").map { it?.value ?: "Curta" }
    val useAi: Flow<Boolean> = dao.getSetting("use_ai").map { it?.value?.toBoolean() ?: true }
    val language: Flow<String> = dao.getSetting("language").map { it?.value ?: if (java.util.Locale.getDefault().language == "pt") "Português" else "English" }

    suspend fun setApiKey(key: String) {
        dao.saveSetting(Setting("api_key", key))
    }

    suspend fun setResponseMode(mode: String) {
        dao.saveSetting(Setting("response_mode", mode))
    }

    suspend fun setUseAi(use: Boolean) {
        dao.saveSetting(Setting("use_ai", use.toString()))
    }

    suspend fun setLanguage(lang: String) {
        dao.saveSetting(Setting("language", lang))
    }
}
