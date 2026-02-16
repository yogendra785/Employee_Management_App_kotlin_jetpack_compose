package com.example.protection.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.protection.domain.model.Employee
import com.example.protection.viewmodel.site.SiteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    viewModel: SiteDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val site by viewModel.site.collectAsState()
    val deployedGuards by viewModel.deployedGuards.collectAsState()
    val availableGuards by viewModel.availableGuards.collectAsState()

    var showDeployDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(site?.name ?: "Site Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDeployDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deploy Guard")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 1. Site Info Card
            site?.let { currentSite ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(currentSite.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(currentSite.address, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Guards: ${deployedGuards.size} / ${currentSite.capacity}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Deployed Guards List
            Text("Deployed Personnel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (deployedGuards.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No guards assigned yet.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deployedGuards) { guard ->
                        DeployedGuardCard(
                            guard = guard,
                            onRemove = { viewModel.removeGuard(guard) }
                        )
                    }
                }
            }
        }
    }

    // 3. Deploy Dialog Popup
    if (showDeployDialog) {
        DeployDialog(
            availableGuards = availableGuards,
            onDismiss = { showDeployDialog = false },
            onSelect = { guard ->
                viewModel.assignGuard(guard)
                showDeployDialog = false
            }
        )
    }
}

@Composable
fun DeployedGuardCard(guard: Employee, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(guard.name.take(1), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(guard.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(guard.employeeId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DeployDialog(
    availableGuards: List<Employee>,
    onDismiss: () -> Unit,
    onSelect: (Employee) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Guard to Deploy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (availableGuards.isEmpty()) {
                    Text("No available guards found.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(availableGuards) { guard ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(guard) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(guard.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(guard.department.ifEmpty { "Unassigned" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    }
}