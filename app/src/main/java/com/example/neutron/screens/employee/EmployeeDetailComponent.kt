package com.example.neutron.screens.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AttendanceSummaryCard(
    present : Int, absent: Int
){
    Card (
        modifier=Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)

    ){
        Row(
            modifier=Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            StateColumn("Present",present, MaterialTheme.colorScheme.primary)
            VerticalDivider(modifier = Modifier.height(40.dp),color = Color.Gray.copy(0.3f))
            StateColumn("Absent",absent, MaterialTheme.colorScheme.primary)


        }
    }
}

@Composable
fun StateColumn(label: String, count: Int, color: Color){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = count.toString(),style = MaterialTheme.typography.displaySmall,color = color)
        Text(text = label , style = MaterialTheme.typography.labelLarge,color = color.copy(0.7f))

    }
}