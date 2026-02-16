package com.example.protection.data.mapper

import com.example.protection.data.local.entity.SalaryEntity
import com.example.protection.domain.model.SalaryRecord

fun SalaryRecord.toEntity(): SalaryEntity {
    return SalaryEntity(
        salaryId = this.id, // 🔹 Map Domain 'id' to Entity 'salaryId'
        employeeId = this.employeeId, // 🔹 String
        month = this.month,
        baseSalary = this.baseSalary,
        advancePaid = this.advancePaid,
        absentDays = this.absentDays,
        perDayDeduction = this.perDayDeduction
    )
}

fun SalaryEntity.toDomain(employeeName: String): SalaryRecord {
    return SalaryRecord(
        id = this.salaryId, // 🔹 Map Entity 'salaryId' to Domain 'id'
        employeeId = this.employeeId, // 🔹 String
        employeeName = employeeName,
        month = this.month,
        baseSalary = this.baseSalary,
        advancePaid = this.advancePaid,
        absentDays = this.absentDays,
        perDayDeduction = this.perDayDeduction,
        netPayable = baseSalary - (absentDays * perDayDeduction) - advancePaid
    )
}