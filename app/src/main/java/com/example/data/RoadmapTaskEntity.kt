package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roadmap_tasks")
data class RoadmapTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phase: Int,
    val phaseTitle: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isAiGenerated: Boolean = false
)
