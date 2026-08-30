package com.ghostpin.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyWaypoint
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "saved_journeys")
data class SavedJourneyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val waypointsJson: String,
    val travelSpeedKmh: Double = 40.0,
    val loopRoute: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Journey {
        val list = mutableListOf<JourneyWaypoint>()
        try {
            val array = JSONArray(waypointsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    JourneyWaypoint(
                        id = obj.optString("id"),
                        name = obj.getString("name"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        stayDurationMinutes = obj.optLong("stayDurationMinutes", 10L),
                        travelTimeToNextMinutes = if (obj.has("travelTimeToNextMinutes") && !obj.isNull("travelTimeToNextMinutes")) {
                            obj.getLong("travelTimeToNextMinutes")
                        } else null
                    )
                )
            }
        } catch (ignored: Exception) {}

        return Journey(
            id = id,
            name = name,
            waypoints = list,
            travelSpeedKmh = travelSpeedKmh,
            loopRoute = loopRoute,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(journey: Journey): SavedJourneyEntity {
            val array = JSONArray()
            journey.waypoints.forEach { wp ->
                val obj = JSONObject().apply {
                    put("id", wp.id)
                    put("name", wp.name)
                    put("latitude", wp.latitude)
                    put("longitude", wp.longitude)
                    put("stayDurationMinutes", wp.stayDurationMinutes)
                    put("travelTimeToNextMinutes", wp.travelTimeToNextMinutes)
                }
                array.put(obj)
            }

            return SavedJourneyEntity(
                id = journey.id,
                name = journey.name,
                waypointsJson = array.toString(),
                travelSpeedKmh = journey.travelSpeedKmh,
                loopRoute = journey.loopRoute,
                createdAt = journey.createdAt
            )
        }
    }
}
