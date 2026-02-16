package com.example.protection.di

import android.content.Context
import com.example.protection.data.local.dao.AttendanceDao
import com.example.protection.data.local.dao.EmployeeDao
import com.example.protection.data.local.dao.LeaveDao
import com.example.protection.data.local.dao.SalaryDao
import com.example.protection.data.local.database.EmployeeDatabase
import com.example.protection.data.repository.SiteRepository
import com.example.protection.data.repository.SiteRepositoryImpl
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

    @Provides
    @Singleton
    fun provideSiteRepository(firestore: FirebaseFirestore): SiteRepository {
        return SiteRepositoryImpl(firestore)
    }


}