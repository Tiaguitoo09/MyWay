package com.example.myway.data.models

data class RecentPlace(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", "", 0.0, 0.0, 0L)


    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "address" to address,
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): RecentPlace {
            return RecentPlace(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                address = map["address"] as? String ?: "",
                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}