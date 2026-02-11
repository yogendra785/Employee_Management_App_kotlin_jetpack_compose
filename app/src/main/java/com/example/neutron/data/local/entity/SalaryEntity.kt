package com.example.neutron.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salaries",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["employeeId"], // Points to the String ID
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId")]
)
data class SalaryEntity(
    @PrimaryKey(autoGenerate = true)
    val salaryId: Long = 0, // 🔹 NOTE: Primary Key is 'salaryId'
    val employeeId: String,
    val month: String,
    val baseSalary: Double,
    val advancePaid: Double,
    val absentDays: Int,
    val perDayDeduction: Double
)