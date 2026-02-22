package com.example.protection.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.protection.domain.model.Attendance
import com.example.protection.domain.model.AttendanceStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthlyHeatmap(
    selectedDate: Long,
    myAttendanceList: List<Attendance>
) {
    // 1. Setup Calendar for the currently selected month
    val calendar = remember(selectedDate) {
        Calendar.getInstance().apply { timeInMillis = selectedDate }
    }

    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

    // 2. Calculate grid offsets (What day of the week does the 1st fall on?)
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
    }
    val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startOffset = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0, Monday = 1, etc.

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Heatmap: $monthName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Day of Week Header (S, M, T...)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // The Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(250.dp), // Fixed height so it doesn't scroll infinitely
                userScrollEnabled = false,
                contentPadding = PaddingValues(4.dp)
            ) {
                // Add blank spaces for the offset
                items(startOffset) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                // Add the actual days
                items(daysInMonth) { dayIndex ->
                    val dayOfMonth = dayIndex + 1

                    // Check if there is an attendance record for this exact day
                    val recordForDay = myAttendanceList.find { attendance ->
                        val recordCal = Calendar.getInstance().apply { timeInMillis = attendance.date }
                        recordCal.get(Calendar.DAY_OF_MONTH) == dayOfMonth &&
                                recordCal.get(Calendar.MONTH) == currentMonth &&
                                recordCal.get(Calendar.YEAR) == currentYear
                    }

                    // Determine color based on status
                    val bgColor = when (recordForDay?.status) {
                        AttendanceStatus.PRESENT -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Light Green
                        AttendanceStatus.ABSENT -> Color(0xFFF44336).copy(alpha = 0.2f) // Light Red
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Gray/Pending
                    }

                    val textColor = when (recordForDay?.status) {
                        AttendanceStatus.PRESENT -> Color(0xFF2E7D32) // Dark Green
                        AttendanceStatus.ABSENT -> Color(0xFFC62828) // Dark Red
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}