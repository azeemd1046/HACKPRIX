package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // ---- Profile Actions ----
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC LIMIT 1")
    fun getLatestProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clearProfiles()

    // ---- Roadmap Actions ----
    @Query("SELECT * FROM roadmap_tasks ORDER BY phase ASC, id ASC")
    fun getRoadmapTasks(): Flow<List<RoadmapTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmapTasks(tasks: List<RoadmapTaskEntity>)

    @Query("UPDATE roadmap_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskStatus(id: Int, completed: Boolean)

    @Query("DELETE FROM roadmap_tasks")
    suspend fun clearRoadmapTasks()
}
