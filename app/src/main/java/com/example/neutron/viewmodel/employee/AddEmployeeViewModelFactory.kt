package com.example.neutron.viewmodel.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neutron.data.repository.EmployeeRepository

class AddEmployeeViewModelFactory (
    private val repository: EmployeeRepository

): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddEmployeeViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return AddEmployeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}