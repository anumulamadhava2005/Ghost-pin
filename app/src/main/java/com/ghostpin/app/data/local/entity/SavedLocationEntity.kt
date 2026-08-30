package com.ghostpin.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ghostpin.app.domain.model.SavedLocation

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): SavedLocation = SavedLocation(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: SavedLocation): SavedLocationEntity = SavedLocationEntity(
            id = domain.id,
            name = domain.name,
            latitude = domain.latitude,
            longitude = domain.longitude,
            createdAt = domain.createdAt
        )
    }
}
