package com.example.protection.data.local.dao

import androidx.room.*
import com.example.protection.data.local.entity.SalaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalary(salary: SalaryEntity)

    // 🔹 FIXED: Uses 'salaryId' for sorting and String 'employeeId' for filtering
    @Query("SELECT * FROM salaries WHERE employeeId = :empId ORDER BY salaryId DESC")
    fun getSalaryHistory(empId: String): Flow<List<SalaryEntity>>

    @Query("SELECT * FROM salaries WHERE month = :month")
    fun getSalariesByMonth(month: String): Flow<List<SalaryEntity>>

    @Query("DELETE FROM salaries WHERE employeeId = :empId")
    suspend fun deleteSalaryByEmployee(empId: String)
}