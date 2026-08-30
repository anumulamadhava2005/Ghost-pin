package com.ghostpin.app.domain.model

data class SavedLocation(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)
