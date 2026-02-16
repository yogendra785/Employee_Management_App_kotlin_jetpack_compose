package com.example.protection.domain.model

data class SalaryRecord (
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val month: String,
    val baseSalary: Double,
    val advancePaid: Double =0.0,
    val absentDays: Int =0,
    val perDayDeduction: Double =0.0,
    val netPayable: Double =0.0

    )