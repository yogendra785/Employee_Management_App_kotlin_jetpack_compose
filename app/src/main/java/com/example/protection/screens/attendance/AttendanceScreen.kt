package com.example.protection.screens.attendance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.protection.domain.model.AttendanceStatus
import com.example.protection.viewmodel.attendance.AttendanceViewModel
import com.example.protection.viewmodel.auth.AuthViewModel
import com.example.protection.viewmodel.employee.EmployeeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val summary by attendanceViewModel.summary.collectAsState()
    val isLoading by attendanceViewModel.isLoading.collectAsState()

    // 🔹 Phase 2 States
    val searchQuery by attendanceViewModel.searchQuery.collectAsState()
    val filterStatus by attendanceViewModel.filterStatus.collectAsState()

    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.firebaseUid

    LaunchedEffect(currentUserId) {
        if (userRole != "ADMIN" && currentUserId != null) {
            attendanceViewModel.loadUserData(currentUserId)
        }
    }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // 1. PREMIUM SUMMARY CARD
            if (userRole == "ADMIN") {
                PremiumSummaryCard(adminPresentCount, adminAbsentCount, employees.size, "Today's Overview")
            } else {
                PremiumSummaryCard(summary.present, summary.absent, summary.total, summary.title)
            }

            // 2. WEEK STRIP HEADER
            WeekStripHeader(selectedDate = selectedDate, onDateSelected = { attendanceViewModel.setDate(it) })

            // 🔹 3. SEARCH & FILTER (ADMIN ONLY)
            if (userRole == "ADMIN") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { attendanceViewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search guard by name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )

                    // Filter Chips & Mark All Present Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = filterStatus == null,
                                onClick = { attendanceViewModel.onFilterChange(null) },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = filterStatus == AttendanceStatus.PRESENT,
                                onClick = { attendanceViewModel.onFilterChange(AttendanceStatus.PRESENT) },
                                label = { Text("Present") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f), selectedLabelColor = Color(0xFF2E7D32))
                            )
                            FilterChip(
                                selected = filterStatus == AttendanceStatus.ABSENT,
                                onClick = { attendanceViewModel.onFilterChange(AttendanceStatus.ABSENT) },
                                label = { Text("Absent") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.2f), selectedLabelColor = Color(0xFFC62828))
                            )
                        }

                        // Mark All Button
                        val pendingEmployees = employees.filter { emp -> attendanceList.none { it.employeeId == emp.employeeId } }
                        if (pendingEmployees.isNotEmpty()) {
                            IconButton(
                                onClick = { attendanceViewModel.markAllPresent(pendingEmployees.map { it.employeeId }) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            ) {
                                Icon(Icons.Default.Checklist, contentDescription = "Mark All Present", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 4. DYNAMIC VIEW (List for Admin, Heatmap for Employee)
            if (userRole == "ADMIN") {
                // ADMIN VIEW: The Swipeable List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filteredList = employees.filter { emp ->
                        val matchesSearch = emp.name.contains(searchQuery, ignoreCase = true)
                        val status = attendanceList.find { it.employeeId == emp.employeeId }?.status
                        val matchesStatus = when (filterStatus) {
                            null -> true
                            AttendanceStatus.PRESENT -> status == AttendanceStatus.PRESENT
                            AttendanceStatus.ABSENT -> status == AttendanceStatus.ABSENT
                            else -> true
                        }
                        matchesSearch && matchesStatus
                    }

                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else if (filteredList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No matching records.", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    items(filteredList, key = { it.id }) { employee ->
                        val attendance = attendanceList.find { it.employeeId == employee.employeeId }
                        SwipeableAttendanceItem(
                            name = employee.name,
                            status = attendance?.status,
                            isAdmin = true,
                            onPresent = { attendanceViewModel.markAttendance(employee.employeeId, AttendanceStatus.PRESENT) },
                            onAbsent = { attendanceViewModel.markAttendance(employee.employeeId, AttendanceStatus.ABSENT) }
                        )
                    }
                }
            } else {
                // 🔹 EMPLOYEE VIEW: The Monthly Heatmap
                // Only pass attendance records that belong to the logged-in user
                val myAttendance = attendanceList.filter { it.employeeId == currentUser?.employeeId }

                MonthlyHeatmap(
                    selectedDate = selectedDate,
                    myAttendanceList = myAttendance
                )
            }
        }
    }
}


@Composable
fun PremiumSummaryCard(
    present: Int,
    absent: Int,
    total: Int,
    title: String
) {
    val pending = total - (present + absent)
    val presentSweep = if (total > 0) (present.toFloat() / total) * 360f else 0f
    val absentSweep = if (total > 0) (absent.toFloat() / total) * 360f else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut Chart
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 24f
                    // Draw Pending (Grey base)
                    drawArc(Color.LightGray.copy(alpha = 0.3f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(strokeWidth))

                    // Draw Absent (Red) - Starts at -90 (top)
                    drawArc(Color(0xFFE53935), startAngle = -90f, sweepAngle = absentSweep, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round))

                    // Draw Present (Green) - Starts where Red ended
                    drawArc(Color(0xFF43A047), startAngle = -90f + absentSweep, sweepAngle = presentSweep, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                }
                Text(text = "$total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Stats Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    StatIndicator(label = "Present", count = present, color = Color(0xFF43A047))
                    StatIndicator(label = "Absent", count = absent, color = Color(0xFFE53935))
                    StatIndicator(label = "Pending", count = pending, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatIndicator(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

// 🔹 COMPONENT: Horizontal Week Strip Header
@Composable
fun WeekStripHeader(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    // Generate 7 days centered around the currently selected date
    val dates = remember(selectedDate) {
        (-3..3).map { offset ->
            Calendar.getInstance().apply {
                timeInMillis = selectedDate
                add(Calendar.DAY_OF_YEAR, offset)
            }.timeInMillis
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(dates) { dateMs ->
            val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time) // "Mon"
            val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(cal.time) // "14"

            // Check if this date matches the selected date (ignoring time)
            val isSelected = Calendar.getInstance().apply { timeInMillis = dateMs }.get(Calendar.DAY_OF_YEAR) ==
                    Calendar.getInstance().apply { timeInMillis = selectedDate }.get(Calendar.DAY_OF_YEAR)

            // 🔹 FIX 1: Explicit InteractionSource to prevent crash
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    // 🚨 This was the fix for the crash:
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null // Disables built-in ripple to avoid compatibility crash
                    ) { onDateSelected(dateMs) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dayOfMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// 🔹 COMPONENT: Attendance List Item (Kept clean)
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
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
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