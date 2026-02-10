package com.example.neutron.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.neutron.domain.model.AttendanceStatus
import com.example.neutron.viewmodel.attendance.AttendanceViewModel
import com.example.neutron.viewmodel.employee.EmployeeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    employeeViewModel: EmployeeViewModel,
    attendanceViewModel: AttendanceViewModel,
    userRole: String,
    onBackClick: () -> Unit = {}
) {
    val employees by employeeViewModel.employees.collectAsState()
    val attendanceList by attendanceViewModel.attendanceList.collectAsState()
    val selectedDate by attendanceViewModel.selectedDate.collectAsState()

    // 🔹 Get current user's UID to filter if they are an employee
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (userRole == "ADMIN") "Staff Attendance" else "My Attendance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // --- Date Selector ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                AttendanceDateHeader(
                    date = selectedDate,
                    onPrevious = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            add(Calendar.DAY_OF_YEAR, -1)
                        }
                        attendanceViewModel.setDate(cal.timeInMillis)
                    },
                    onNext = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        attendanceViewModel.setDate(cal.timeInMillis)
                    },
                    onToday = {
                        attendanceViewModel.setDate(AttendanceViewModel.startOfToday())
                    }
                )
            }

            // --- Stats / Info Bar ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (userRole == "ADMIN") {
                    Text("${employees.size} Total Employees", color = MaterialTheme.colorScheme.outline)
                    val presentCount = attendanceList.count { it.status == AttendanceStatus.PRESENT }
                    Text("Present Today: $presentCount", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("Daily status for your profile", color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Data List Logic ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🔹 Filter logic: Admin sees all, Employee sees only themselves
                val displayList = if (userRole == "ADMIN") {
                    employees
                } else {
                    employees.filter { it.firebaseUid == currentUserId }
                }

                items(displayList, key = { it.id }) { employee ->
                    val attendance = attendanceList.find { it.employeeId == employee.id }

                    AttendanceItemCard(
                        name = employee.name,
                        status = attendance?.status,
                        isLocked = attendance != null,
                        isAdmin = userRole == "ADMIN",
                        onPresent = {
                            if (userRole == "ADMIN") {
                                attendanceViewModel.markAttendance(employee.id, AttendanceStatus.PRESENT)
                            }
                        },
                        onAbsent = {
                            if (userRole == "ADMIN") {
                                attendanceViewModel.markAttendance(employee.id, AttendanceStatus.ABSENT)
                            }
                        }
                    )
                }
            }
        }
    }
}

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

            // --- Role Based Actions ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAdmin) {
                    // 🔹 Admin View: Interaction Buttons
                    AttendanceChip(
                        label = "Present",
                        isSelected = status == AttendanceStatus.PRESENT,
                        color = Color(0xFF4CAF50),
                        onClick = onPresent
                    )
                    AttendanceChip(
                        label = "Absent",
                        isSelected = status == AttendanceStatus.ABSENT,
                        color = Color(0xFFF44336),
                        onClick = onAbsent
                    )
                } else {
                    // 🔹 Employee View: Read-Only Status Label
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

                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
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
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        )
    )
}