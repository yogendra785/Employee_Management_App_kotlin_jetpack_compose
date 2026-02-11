package com.example.neutron.screens.attendance

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
import com.example.neutron.domain.model.AttendanceStatus
import com.example.neutron.viewmodel.attendance.AttendanceViewModel
import com.example.neutron.viewmodel.auth.AuthViewModel // 🔹 Added
import com.example.neutron.viewmodel.employee.EmployeeViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    employeeViewModel: EmployeeViewModel,
    attendanceViewModel: AttendanceViewModel,
    authViewModel: AuthViewModel, // 🔹 Added Parameter
    userRole: String,
    onBackClick: () -> Unit = {}
) {
    val employees by employeeViewModel.employees.collectAsState()
    val attendanceList by attendanceViewModel.attendanceList.collectAsState()
    val selectedDate by attendanceViewModel.selectedDate.collectAsState()
    val isLoading by attendanceViewModel.isLoading.collectAsState()

    // 🔹 FIX: Get UID from the ViewModel State (which is correct after login)
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.firebaseUid

    LaunchedEffect(currentUserId) {
        Log.d("ATTENDANCE_UI", "Screen Opened. UID: $currentUserId")

        if (userRole != "ADMIN" && currentUserId != null) {
            Log.d("ATTENDANCE_UI", "Triggering Load for $currentUserId")
            attendanceViewModel.loadUserData(currentUserId)
        }
    }

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
            // Date Selector
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))) {
                AttendanceDateHeader(
                    date = selectedDate,
                    onPrevious = { val cal = Calendar.getInstance().apply { timeInMillis = selectedDate; add(Calendar.DAY_OF_YEAR, -1) }; attendanceViewModel.setDate(cal.timeInMillis) },
                    onNext = { val cal = Calendar.getInstance().apply { timeInMillis = selectedDate; add(Calendar.DAY_OF_YEAR, 1) }; attendanceViewModel.setDate(cal.timeInMillis) },
                    onToday = { attendanceViewModel.setDate(AttendanceViewModel.startOfToday()) }
                )
            }

            // Stats Bar
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (userRole == "ADMIN") {
                    Text("${employees.size} Total Employees", color = MaterialTheme.colorScheme.outline)
                } else {
                    if(isLoading) Text("Syncing...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    else Text("Status: Updated", color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val displayList = if (userRole == "ADMIN") employees else employees.filter { it.firebaseUid == currentUserId }

                if (displayList.isEmpty() && !isLoading) {
                    item { Text("No record found. Try refreshing.", modifier = Modifier.padding(16.dp)) }
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
// (Keep AttendanceItemCard / AttendanceChip components below)
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