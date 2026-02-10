package com.example.neutron.di

import android.content.Context
import com.example.neutron.data.auth.AuthRepository
import com.example.neutron.data.local.dao.AttendanceDao
import com.example.neutron.data.local.dao.EmployeeDao
import com.example.neutron.data.local.dao.LeaveDao
import com.example.neutron.data.local.dao.SalaryDao
import com.example.neutron.data.local.database.EmployeeDatabase
import com.example.neutron.data.repository.AttendanceRepository
import com.example.neutron.data.repository.EmployeeRepository
import com.example.neutron.data.repository.LeaveRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Firebase Providers ---

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- Database Providers ---

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EmployeeDatabase {
        return EmployeeDatabase.getDatabase(context)
    }

    @Provides
    fun provideEmployeeDao(db: EmployeeDatabase): EmployeeDao = db.employeeDao()

    @Provides
    fun provideAttendanceDao(db: EmployeeDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideSalaryDao(db: EmployeeDatabase): SalaryDao = db.salaryDao()

    @Provides
    fun provideLeaveDao(db: EmployeeDatabase): LeaveDao = db.leaveDao()

    // --- Repository Providers ---

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepository(auth)
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(dao: AttendanceDao): AttendanceRepository {
        return AttendanceRepository(dao)
    }

    /**
     * 🔹 FIXED: Added 'firestore' parameter to match LeaveRepository constructor.
     */
    @Provides
    @Singleton
    fun provideLeaveRepository(
        dao: LeaveDao,
        firestore: FirebaseFirestore
    ): LeaveRepository {
        return LeaveRepository(dao, firestore)
    }

    /**
     * 🔹 FIXED: Added 'leaveDao' parameter to match updated EmployeeRepository constructor.
     */
    @Provides
    @Singleton
    fun provideEmployeeRepository(
        employeeDao: EmployeeDao,
        salaryDao: SalaryDao,
        attendanceDao: AttendanceDao,
        leaveDao: LeaveDao,
        @ApplicationContext context: Context
    ): EmployeeRepository {
        return EmployeeRepository(
            dao = employeeDao,
            salaryDao = salaryDao,
            attendanceDao = attendanceDao,
            leaveDao = leaveDao,
            context = context
        )
    }
}