package com.example.protection.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.protection.domain.model.Site
import com.example.protection.viewmodel.site.SiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteListScreen(
    viewModel: SiteViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSiteClick: (String) -> Unit // We will use this later for the Detail Screen
) {
    val sites by viewModel.sites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Sites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Site", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (sites.isEmpty() && !isLoading) {
                // Empty State
                Text(
                    text = "No sites added yet.\nClick + to add a location.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sites) { site ->
                        SiteItemCard(
                            site = site,
                            onClick = { onSiteClick(site.id) },
                            onDelete = { viewModel.deleteSite(site.id) }
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showAddDialog) {
        AddSiteDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, address, cap ->
                viewModel.addSite(name, address, cap)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SiteItemCard(site: Site, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(site.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(site.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))

                // Status Pill (Simple for now)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Capacity: ${site.capacity} Guards",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddSiteDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add New Site", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Site Name (e.g. City Mall)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = capacity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) capacity = it },
                    label = { Text("Required Guards") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAdd(name, address, capacity) },
                        enabled = name.isNotBlank() && address.isNotBlank()
                    ) {
                        Text("Add Site")
                    }
                }
            }
        }
    }
}