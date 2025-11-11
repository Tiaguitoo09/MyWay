package com.example.myway.screens.modulo3

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlin.math.roundToInt

object PlacesCache {
    private const val PREFS_NAME = "places_cache_prefs"
    private const val CACHE_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L // 7 días
    private val gson = Gson()

    data class CachedPlaces(
        val places: List<SerializableNearbyPlace>,
        val timestamp: Long
    )

    data class SerializableNearbyPlace(
        val placeId: String,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val type: String
    ) {
        fun toNearbyPlace() = NearbyPlace(placeId, name, LatLng(latitude, longitude), type)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun generateKey(lat: Double, lng: Double, type: String): String {
        val roundedLat = (lat * 100).roundToInt() / 100.0
        val roundedLng = (lng * 100).roundToInt() / 100.0
        return "${roundedLat},${roundedLng}|$type"
    }

    fun get(context: Context, lat: Double, lng: Double, type: String): List<NearbyPlace>? {
        val key = generateKey(lat, lng, type)
        val prefs = getPrefs(context)
        val json = prefs.getString(key, null) ?: return null

        return try {
            val cached = gson.fromJson(json, CachedPlaces::class.java)

            val currentTime = System.currentTimeMillis()
            if (currentTime - cached.timestamp > CACHE_EXPIRY_MS) {
                prefs.edit().remove(key).apply()
                android.util.Log.d("PlacesCache", "⏰ Caché de lugares expirado")
                null
            } else {
                android.util.Log.d("PlacesCache", "✅ Lugares desde caché (GRATIS)")
                cached.places.map { it.toNearbyPlace() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun put(context: Context, lat: Double, lng: Double, type: String, places: List<NearbyPlace>) {
        val key = generateKey(lat, lng, type)

        val serializablePlaces = places.map {
            SerializableNearbyPlace(
                placeId = it.placeId,
                name = it.name,
                latitude = it.latLng.latitude,
                longitude = it.latLng.longitude,
                type = it.type
            )
        }

        val cached = CachedPlaces(
            places = serializablePlaces,
            timestamp = System.currentTimeMillis()
        )

        val json = gson.toJson(cached)
        getPrefs(context).edit().putString(key, json).apply()
        android.util.Log.d("PlacesCache", "💾 Lugares guardados en caché")
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
        android.util.Log.d("PlacesCache", "🗑️ Caché de lugares eliminado")
    }
}