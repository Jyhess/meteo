package com.meteo.app.domain

data class SavedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false
)
