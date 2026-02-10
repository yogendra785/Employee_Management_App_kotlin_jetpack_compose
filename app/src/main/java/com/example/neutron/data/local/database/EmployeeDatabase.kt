package com.example.neutron.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase // 🔹 Added for Callback
import com.example.neutron.data.local.dao.AttendanceDao
import com.example.neutron.data.local.dao.EmployeeDao
import com.example.neutron.data.local.dao.LeaveDao
import com.example.neutron.data.local.dao.SalaryDao
import com.example.neutron.data.local.entity.AttendanceEntity
import com.example.neutron.data.local.entity.EmployeeEntity
import com.example.neutron.data.local.entity.LeaveEntity
import com.example.neutron.data.local.entity.SalaryEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceEntity::class,
        LeaveEntity::class,
        SalaryEntity::class
    ],
    version = 13, // 🔹 Incremented to match your current schema requirements
    exportSchema = false
)
abstract class EmployeeDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun salaryDao(): SalaryDao

    companion object {
        @Volatile
        private var INSTANCE: EmployeeDatabase? = null

        fun getDatabase(context: Context): EmployeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmployeeDatabase::class.java,
                    "employee_db"
                )
                    .fallbackToDestructiveMigration()
                    // 🔹 Using a Room Callback is cleaner for pre-populating data
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            insertAdmin(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun insertAdmin(db: SupportSQLiteDatabase) {
            Executors.newSingleThreadExecutor().execute {
                try {
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO employees (
                            id, 
                            employeeId, 
                            firebaseUid, 
                            name, 
                            email, 
                            role, 
                            department, 
                            salary, 
                            isActive, 
                            createdAt, 
                            password
                        ) 
                        VALUES (
                            1, 
                            'ADMIN001', 
                            'ADMIN_UID', 
                            'Yogendra', 
                            'admin@neutron.com', 
                            'ADMIN', 
                            'IT', 
                            0.0, 
                            1, 
                            ${System.currentTimeMillis()}, 
                            '123456'
                        )
                        """.trimIndent()
                    )
                    Log.d("NEUTRON_DB", "Admin user verified/inserted with employeeId successfully")
                } catch (e: Exception) {
                    Log.e("NEUTRON_DB", "Manual insertion failed: ${e.message}")
                }
            }
        }
    }
}