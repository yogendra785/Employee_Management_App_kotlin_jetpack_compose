package com.example.protection.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["employeeId"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("employeeId"),
        // Prevents duplicate entries for the same employee on the same day
        Index(value = ["employeeId", "date"], unique = true)
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val date: Long, // Stored as Timestamp
    val status: String // e.g., "Present", "Absent", "Late"
)