package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coaching_profile")
data class CoachingProfile(
    @PrimaryKey val id: Int = 1,
    val coachingName: String = "Apex Coaching Classes",
    val teacherName: String = "Director / Faculty",
    val mobileNumber: String = "9876543210",
    val address: String = "Station Road, City Center",
    val currencySymbol: String = "₹"
)
