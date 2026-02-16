package com.example.protection.data.repository

import com.example.protection.domain.model.Site
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    // 1. Get a live list of all sites
    fun getAllSites(): Flow<List<Site>>

    // 2. Add or Update a site
    suspend fun saveSite(site: Site)

    // 3. Delete a site
    suspend fun deleteSite(siteId: String)

    // 4. Get a specific site by ID (for the Detail Screen)
    suspend fun getSiteById(siteId: String): Site?
}