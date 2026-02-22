package com.example.protection.screens.attendance


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.protection.domain.model.AttendanceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableAttendanceItem(
    name: String,
    status: AttendanceStatus?,
    isAdmin: Boolean,
    onPresent: () -> Unit,
    onAbsent: () -> Unit
) {
    // If not admin, just show the static card (No swiping)
    if (!isAdmin) {
        AttendanceItemCard(name, status, isLocked = status != null, isAdmin = false, onPresent = {}, onAbsent = {})
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> { // Swipe Right -> Present
                    onPresent()
                    false // Don't remove item from list, just snap back
                }
                SwipeToDismissBoxValue.EndToStart -> { // Swipe Left -> Absent
                    onAbsent()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF43A047) // Green
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935) // Red
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(Icons.Default.Check, contentDescription = "Present", tint = Color.White)
                } else if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.Close, contentDescription = "Absent", tint = Color.White)
                }
            }
        },
        content = {
            AttendanceItemCard(
                name = name,
                status = status,
                isLocked = status != null, // Visual feedback if already marked
                isAdmin = true,
                onPresent = onPresent, // Keep buttons as backup
                onAbsent = onAbsent
            )
        }
    )
}