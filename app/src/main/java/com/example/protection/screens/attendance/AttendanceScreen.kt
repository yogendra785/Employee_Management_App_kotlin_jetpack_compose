package com.example.protection.screens.attendance

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protection.domain.model.AttendanceStatus
import com.example.protection.viewmodel.attendance.AttendanceViewModel
import com.example.protection.viewmodel.auth.AuthViewModel
import com.example.protection.viewmodel.employee.EmployeeViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    employeeViewModel: EmployeeViewModel,
    attendanceViewModel: AttendanceViewModel,
    authViewModel: AuthViewModel,
    userRole: String,
    onBackClick: () -> Unit = {}
) {
    val employees by employeeViewModel.employees.collectAsState()
    val attendanceList by attendanceViewModel.attendanceList.collectAsState()
    val selectedDate by attendanceViewModel.selectedDate.collectAsState()
    val summary by attendanceViewModel.summary.collectAsState() // 🔹 Collect Summary
    val isLoading by attendanceViewModel.isLoading.collectAsState()

    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.firebaseUid

    LaunchedEffect(currentUserId) {
        if (userRole != "ADMIN" && currentUserId != null) {
            attendanceViewModel.loadUserData(currentUserId)
        }
    }

    // 🔹 For Admin: Calculate Daily Stats on the fly from the displayed list
    // (Because the ViewModel 'summary' is optimized for the logged-in user's monthly view)
    val adminPresentCount = if (userRole == "ADMIN") attendanceList.count { it.status == AttendanceStatus.PRESENT } else 0
    val adminAbsentCount = if (userRole == "ADMIN") attendanceList.count { it.status == AttendanceStatus.ABSENT } else 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (userRole == "ADMIN") "Staff Attendance" else "My Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface)
        ) {
            // 🔹 1. SUMMARY CARD
            if (userRole == "ADMIN") {
                // Admin sees "Today's Overview"
                AttendanceSummaryCard(
                    present = adminPresentCount,
                    absent = adminAbsentCount,
                    title = "Today's Overview"
                )
            } else {
                // Employee sees "My Monthly Record" (from ViewModel)
                AttendanceSummaryCard(
                    present = summary.present,
                    absent = summary.absent,
                    title = summary.title
                )
            }

            // 🔹 2. DATE SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                AttendanceDateHeader(
                    date = selectedDate,
                    onPrevious = { val cal = Calendar.getInstance().apply { timeInMillis = selectedDate; add(Calendar.DAY_OF_YEAR, -1) }; attendanceViewModel.setDate(cal.timeInMillis) },
                    onNext = { val cal = Calendar.getInstance().apply { timeInMillis = selectedDate; add(Calendar.DAY_OF_YEAR, 1) }; attendanceViewModel.setDate(cal.timeInMillis) },
                    onToday = { attendanceViewModel.setDate(AttendanceViewModel.startOfToday()) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 3. LIST
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val displayList = if (userRole == "ADMIN") employees else employees.filter { it.firebaseUid == currentUserId }

                if (displayList.isEmpty() && !isLoading) {
                    item { Text("No record found.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.outline) }
                }

                items(displayList, key = { it.id }) { employee ->
                    val attendance = attendanceList.find { it.employeeId == employee.employeeId }
                    AttendanceItemCard(
                        name = employee.name,
                        status = attendance?.status,
                        isLocked = attendance != null,
                        isAdmin = userRole == "ADMIN",
                        onPresent = { if (userRole == "ADMIN") attendanceViewModel.markAttendance(employee.employeeId, AttendanceStatus.PRESENT) },
                        onAbsent = { if (userRole == "ADMIN") attendanceViewModel.markAttendance(employee.employeeId, AttendanceStatus.ABSENT) }
                    )
                }
            }
        }
    }
}

// 🔹 NEW COMPONENT: Attendance Summary Card
@Composable
fun AttendanceSummaryCard(
    present: Int,
    absent: Int,
    title: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(count = present, label = "Present", color = Color(0xFF4CAF50)) // Green
                SummaryItem(count = absent, label = "Absent", color = Color(0xFFF44336))  // Red
                SummaryItem(count = present + absent, label = "Total", color = MaterialTheme.colorScheme.primary) // Blue
            }
        }
    }
}

@Composable
fun SummaryItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

// ... (Keep AttendanceItemCard and AttendanceChip exactly as before) ...
@Composable
fun AttendanceItemCard(
    name: String,
    status: AttendanceStatus?,
    isLocked: Boolean,
    isAdmin: Boolean,
    onPresent: () -> Unit,
    onAbsent: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(45.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAdmin) {
                    AttendanceChip("P", status == AttendanceStatus.PRESENT, Color(0xFF4CAF50), onPresent)
                    AttendanceChip("A", status == AttendanceStatus.ABSENT, Color(0xFFF44336), onAbsent)
                } else {
                    val statusText = when (status) {
                        AttendanceStatus.PRESENT -> "PRESENT"
                        AttendanceStatus.ABSENT -> "ABSENT"
                        else -> "PENDING"
                    }
                    val statusColor = when (status) {
                        AttendanceStatus.PRESENT -> Color(0xFF2E7D32)
                        AttendanceStatus.ABSENT -> Color(0xFFD32F2F)
                        else -> MaterialTheme.colorScheme.outline
                    }
                    Text(statusText, color = statusColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.2f), selectedLabelColor = color)
    )
}