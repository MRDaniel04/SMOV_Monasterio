package com.nextapp.monasterio.ui.screens.pinEdition.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppStatus {
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun startUpload() { _isUploading.value = true }
    fun finishUpload() { _isUploading.value = false }
}