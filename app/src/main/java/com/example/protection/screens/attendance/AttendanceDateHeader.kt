package com.example.protection.screens.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.protection.utils.formatDate

@Composable
fun AttendanceDateHeader(
    date: Long,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(
                    onClick = onToday,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Today", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(onClick = onNext) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
            }
        }
    }
}