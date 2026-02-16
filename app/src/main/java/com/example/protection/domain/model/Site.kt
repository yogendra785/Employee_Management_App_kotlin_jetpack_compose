package com.example.protection.domain.model

data class Site(
    val id: String = "",             // Unique ID (e.g., "SITE_001")
    val name: String = "",           // e.g., "City Mall Main Gate"
    val address: String = "",        // e.g., "Sector 18, Jaipur"
    val city: String = "",           // e.g., "Jaipur"
    val contactPerson: String = "",  // e.g., "Mr. Sharma (Manager)"
    val contactPhone: String = "",   // e.g., "9876543210"
    val capacity: Int = 0,           // How many guards are needed?
    val activeGuards: Int = 0,       // (Optional) We can calculate this dynamically later
    val createdAt: Long = System.currentTimeMillis()
)