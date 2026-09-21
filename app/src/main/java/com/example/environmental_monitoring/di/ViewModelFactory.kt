package com.example.environmental_monitoring.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * Factory genérica simple: recibe un lambda que construye el ViewModel
 * usando el [AppContainer], evitando depender de Hilt.
 */
class ViewModelFactory(
    private val container: AppContainer,
    private val creator: (AppContainer) -> ViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return creator(container) as T
    }
}
