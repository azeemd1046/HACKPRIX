package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val email: String, // email is unique, used as primary key & id
    val fullName: String,
    val branch: String,
    val year: String,
    val careerGoal: String,
    val currentLevel: String,
    val interests: String, // Comma-separated list (e.g. "Web Development,UI/UX")
    val createdAt: Long = System.currentTimeMillis()
)
