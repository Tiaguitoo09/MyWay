package com.example.myway.screens.modulo3

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlin.math.roundToInt

object RouteCache {
    private const val PREFS_NAME = "route_cache_prefs"
    private const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 horas
    private val gson = Gson()

    data class CachedRoute(
        val points: List<SerializableLatLng>,
        val steps: List<NavigationStep>,
        val timestamp: Long
    )

    data class SerializableLatLng(
        val latitude: Double,
        val longitude: Double
    ) {
        fun toLatLng() = LatLng(latitude, longitude)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun generateKey(origin: LatLng, destination: LatLng, mode: String): String {
        val oLat = (origin.latitude * 1000).roundToInt() / 1000.0
        val oLng = (origin.longitude * 1000).roundToInt() / 1000.0
        val dLat = (destination.latitude * 1000).roundToInt() / 1000.0
        val dLng = (destination.longitude * 1000).roundToInt() / 1000.0
        return "${oLat},${oLng}|${dLat},${dLng}|$mode"
    }

    fun get(context: Context, origin: LatLng, destination: LatLng, mode: String): Pair<List<LatLng>, List<NavigationStep>>? {
        val key = generateKey(origin, destination, mode)
        val prefs = getPrefs(context)
        val json = prefs.getString(key, null) ?: return null

        return try {
            val cached = gson.fromJson(json, CachedRoute::class.java)

            val currentTime = System.currentTimeMillis()
            if (currentTime - cached.timestamp > CACHE_EXPIRY_MS) {
                prefs.edit().remove(key).apply()
                android.util.Log.d("RouteCache", "❌ Caché expirado")
                null
            } else {
                android.util.Log.d("RouteCache", "✅ Caché encontrado (GRATIS)")
                val points = cached.points.map { it.toLatLng() }
                Pair(points, cached.steps)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun put(context: Context, origin: LatLng, destination: LatLng, mode: String,
            points: List<LatLng>, steps: List<NavigationStep>) {
        val key = generateKey(origin, destination, mode)

        val serializablePoints = points.map {
            SerializableLatLng(it.latitude, it.longitude)
        }

        val cached = CachedRoute(
            points = serializablePoints,
            steps = steps,
            timestamp = System.currentTimeMillis()
        )

        val json = gson.toJson(cached)
        getPrefs(context).edit().putString(key, json).apply()
        android.util.Log.d("RouteCache", "💾 Ruta guardada en caché")
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
        android.util.Log.d("RouteCache", "🗑️ Caché eliminado")
    }
}