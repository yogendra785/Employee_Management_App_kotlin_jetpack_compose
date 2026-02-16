package com.example.protection.viewmodel.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.SiteRepository
import com.example.protection.domain.model.Site
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val repository: SiteRepository
) : ViewModel() {

    // 1. Holds the list of sites
    private val _sites = MutableStateFlow<List<Site>>(emptyList())
    val sites: StateFlow<List<Site>> = _sites.asStateFlow()

    // 2. Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSites()
    }

    private fun loadSites() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllSites()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { list ->
                    _sites.value = list
                    _isLoading.value = false
                }
        }
    }

    fun addSite(name: String, address: String, capacity: String) {
        viewModelScope.launch {
            if (name.isBlank() || address.isBlank()) {
                _error.value = "Name and Address are required"
                return@launch
            }

            val cap = capacity.toIntOrNull() ?: 0
            val newSite = Site(name = name, address = address, capacity = cap)

            repository.saveSite(newSite)
        }
    }

    fun deleteSite(siteId: String) {
        viewModelScope.launch {
            repository.deleteSite(siteId)
        }
    }

    fun clearError() { _error.value = null }
}