package com.example.neutron.data.local.dao

import androidx.room.*
import com.example.neutron.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    /**
     * Used for the UI list. Automatically updates the screen
     * whenever an employee is added, removed, or updated.
     */
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployeesFlow(): Flow<List<EmployeeEntity>>

    /**
     * Used for background calculations (Dashboard Analytics).
     * Must be 'suspend' so it doesn't block the main thread.
     */
    @Query("SELECT * FROM employees")
    suspend fun getAllEmployeesList(): List<EmployeeEntity>

    @Query("SELECT * FROM employees WHERE id = :employeeId LIMIT 1")
    fun getEmployeeById(employeeId: Long): Flow<EmployeeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 🔹 Use REPLACE to handle updates smoothly
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM employees WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean

    @Query("UPDATE employees SET isActive = :isActive WHERE id = :employeeId")
    suspend fun updateEmployeeStatus(employeeId: Long, isActive: Boolean)
}