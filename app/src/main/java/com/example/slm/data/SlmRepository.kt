package com.example.slm.data

import com.example.slm.model.SlmConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SlmRepository(private val profileDao: SlmProfileDao) {

    private val _activeConfig = MutableStateFlow(
        SlmConfig(
            renderWidth = 720,
            renderHeight = 2460,
            nativeWidth = 1080,
            nativeHeight = 2460,
            stretchFactorX = 1.50f,
            stretchFactorY = 1.00f
        )
    )
    val activeConfig: StateFlow<SlmConfig> = _activeConfig.asStateFlow()

    fun getAllProfiles(): Flow<List<SlmProfileEntity>> = profileDao.getAllProfilesFlow()

    suspend fun loadProfile(name: String, nativeW: Int, nativeH: Int): SlmConfig? = withContext(Dispatchers.IO) {
        val entity = profileDao.getProfileByName(name)
        val config = entity?.toConfig(nativeW, nativeH)
        if (config != null) {
            _activeConfig.value = config
        }
        config
    }

    suspend fun saveProfile(name: String, description: String, config: SlmConfig): Long = withContext(Dispatchers.IO) {
        val entity = SlmProfileEntity.fromConfig(name, description, config, isPreset = false)
        profileDao.insertProfile(entity)
    }

    suspend fun deleteProfile(profile: SlmProfileEntity) = withContext(Dispatchers.IO) {
        if (!profile.isPreset) {
            profileDao.deleteProfile(profile)
        }
    }

    suspend fun restoreDefaultProfiles() = withContext(Dispatchers.IO) {
        profileDao.clearAll()
        profileDao.insertAll(SlmDatabase.DEFAULT_PRESETS)
        _activeConfig.value = SlmDatabase.DEFAULT_PRESETS[1].toConfig()
    }

    fun updateActiveConfig(config: SlmConfig) {
        _activeConfig.value = config
    }
}
