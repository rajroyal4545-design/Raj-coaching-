package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CoachingProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachingProfileDao {
    @Query("SELECT * FROM coaching_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<CoachingProfile?>

    @Query("SELECT * FROM coaching_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): CoachingProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: CoachingProfile)
}
