package com.example.slm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SlmProfileDao {
    @Query("SELECT * FROM slm_profiles ORDER BY isPreset DESC, name ASC")
    fun getAllProfilesFlow(): Flow<List<SlmProfileEntity>>

    @Query("SELECT * FROM slm_profiles WHERE name = :name LIMIT 1")
    suspend fun getProfileByName(name: String): SlmProfileEntity?

    @Query("SELECT * FROM slm_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): SlmProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SlmProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<SlmProfileEntity>)

    @Update
    suspend fun updateProfile(profile: SlmProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: SlmProfileEntity)

    @Query("DELETE FROM slm_profiles WHERE isPreset = 0")
    suspend fun deleteAllCustomProfiles()

    @Query("DELETE FROM slm_profiles")
    suspend fun clearAll()
}
