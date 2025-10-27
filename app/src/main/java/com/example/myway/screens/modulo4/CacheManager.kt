package com.example.myway.data

import android.content.Context
import android.util.Log
import com.example.myway.ai.Place
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.myway.ai.toMap
import java.util.concurrent.TimeUnit

/**
 * Gestor de caché inteligente para lugares
 *
 * Estrategia:
 * - Caché en memoria (RAM) para acceso instantáneo
 * - Caché en SharedPreferences para datos entre sesiones
 * - Firestore solo para lugares curados (no búsquedas)
 * - Expiración automática de datos
 */
class CacheManager(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("places_cache", Context.MODE_PRIVATE)

    // Caché en memoria (más rápido)
    private val memoryCache = mutableMapOf<String, CacheEntry>()

    companion object {
        private const val TAG = "CacheManager"

        // Tiempos de expiración
        private val FIREBASE_CACHE_DURATION = TimeUnit.HOURS.toMillis(24) // 24 horas
        private val GOOGLE_PLACES_CACHE_DURATION = TimeUnit.MINUTES.toMillis(30) // 30 min
        private val MEMORY_CACHE_DURATION = TimeUnit.MINUTES.toMillis(10) // 10 min

        // Claves de caché
        const val KEY_FIREBASE_PLACES = "firebase_places"
        const val KEY_GOOGLE_NEARBY = "google_nearby"
    }

    /**
     * Entrada de caché con timestamp
     */
    private data class CacheEntry(
        val places: List<Place>,
        val timestamp: Long,
        val expiresDuration: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > expiresDuration
        }
    }

    // ========== CACHÉ DE LUGARES DE FIREBASE (24h) ==========

    /**
     * Guardar lugares curados de Firebase (tu catálogo estático)
     */
    suspend fun cacheFirebasePlaces(places: List<Place>) {
        try {
            // 1. Memoria
            memoryCache[KEY_FIREBASE_PLACES] = CacheEntry(
                places = places,
                timestamp = System.currentTimeMillis(),
                expiresDuration = FIREBASE_CACHE_DURATION
            )

            // 2. SharedPreferences (persistente)
            saveToPrefs(KEY_FIREBASE_PLACES, places, FIREBASE_CACHE_DURATION)

            Log.d(TAG, "✅ ${places.size} lugares de Firebase en caché")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando caché Firebase: ${e.message}")
        }
    }

    /**
     * Obtener lugares de Firebase desde caché
     */
    fun getFirebasePlacesCache(): List<Place>? {
        // 1. Intentar memoria primero
        memoryCache[KEY_FIREBASE_PLACES]?.let { entry ->
            if (!entry.isExpired()) {
                Log.d(TAG, "📦 Cache hit (memoria): Firebase places")
                return entry.places
            }
        }

        // 2. Intentar SharedPreferences
        return getFromPrefs(KEY_FIREBASE_PLACES, FIREBASE_CACHE_DURATION)?.also {
            Log.d(TAG, "📦 Cache hit (prefs): Firebase places")
            // Restaurar a memoria
            memoryCache[KEY_FIREBASE_PLACES] = CacheEntry(
                places = it,
                timestamp = System.currentTimeMillis(),
                expiresDuration = FIREBASE_CACHE_DURATION
            )
        }
    }

    // ========== CACHÉ DE GOOGLE PLACES (30 min) ==========

    /**
     * Guardar resultados de Google Places (temporal, 30 min)
     */
    fun cacheGooglePlaces(locationKey: String, places: List<Place>) {
        try {
            val key = "$KEY_GOOGLE_NEARBY-$locationKey"

            // Solo caché en memoria (no persistir búsquedas)
            memoryCache[key] = CacheEntry(
                places = places,
                timestamp = System.currentTimeMillis(),
                expiresDuration = GOOGLE_PLACES_CACHE_DURATION
            )

            Log.d(TAG, "✅ ${places.size} lugares de Google en caché (memoria)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando caché Google: ${e.message}")
        }
    }

    /**
     * Obtener lugares de Google Places desde caché
     */
    fun getGooglePlacesCache(locationKey: String): List<Place>? {
        val key = "$KEY_GOOGLE_NEARBY-$locationKey"

        return memoryCache[key]?.let { entry ->
            if (!entry.isExpired()) {
                Log.d(TAG, "📦 Cache hit (memoria): Google places [$locationKey]")
                entry.places
            } else {
                Log.d(TAG, "⏰ Cache expirado: Google places [$locationKey]")
                memoryCache.remove(key)
                null
            }
        }
    }

    // ========== HELPERS PRIVADOS ==========

    /**
     * Guardar en SharedPreferences
     */
    private fun saveToPrefs(key: String, places: List<Place>, duration: Long) {
        try {
            val json = placesToJson(places)
            prefs.edit()
                .putString("$key-data", json)
                .putLong("$key-timestamp", System.currentTimeMillis())
                .putLong("$key-duration", duration)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando en prefs: ${e.message}")
        }
    }

    /**
     * Obtener de SharedPreferences
     */
    private fun getFromPrefs(key: String, duration: Long): List<Place>? {
        try {
            val timestamp = prefs.getLong("$key-timestamp", 0L)
            if (timestamp == 0L) return null

            // Verificar expiración
            if (System.currentTimeMillis() - timestamp > duration) {
                Log.d(TAG, "⏰ Cache expirado en prefs: $key")
                clearPrefsKey(key)
                return null
            }

            val json = prefs.getString("$key-data", null) ?: return null
            return jsonToPlaces(json)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error leyendo de prefs: ${e.message}")
            return null
        }
    }

    /**
     * Limpiar clave específica de SharedPreferences
     */
    private fun clearPrefsKey(key: String) {
        prefs.edit()
            .remove("$key-data")
            .remove("$key-timestamp")
            .remove("$key-duration")
            .apply()
    }

    // ========== SERIALIZACIÓN JSON ==========

    private fun placesToJson(places: List<Place>): String {
        // Convertir lista de lugares a JSON simple
        val maps = places.map { it.toMap() }
        return com.google.gson.Gson().toJson(maps)
    }

    private fun jsonToPlaces(json: String): List<Place> {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
        val maps: List<Map<String, Any?>> = gson.fromJson(json, type)
        return maps.map { Place.fromMap(it) }
    }

    // ========== LIMPIEZA ==========

    /**
     * Limpiar caché en memoria
     */
    fun clearMemoryCache() {
        memoryCache.clear()
        Log.d(TAG, "🧹 Caché en memoria limpiado")
    }

    /**
     * Limpiar todo el caché (memoria + prefs)
     */
    fun clearAllCache() {
        memoryCache.clear()
        prefs.edit().clear().apply()
        Log.d(TAG, "🧹 Todo el caché limpiado")
    }

    /**
     * Limpiar caché expirado automáticamente
     */
    fun cleanExpiredCache() {
        // Limpiar memoria
        val expiredKeys = memoryCache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { memoryCache.remove(it) }

        if (expiredKeys.isNotEmpty()) {
            Log.d(TAG, "🧹 Limpiados ${expiredKeys.size} items expirados de memoria")
        }
    }

    /**
     * Generar clave de ubicación para caché
     */
    fun generateLocationKey(lat: Double, lon: Double, radiusKm: Double): String {
        // Redondear a 2 decimales para agrupar búsquedas cercanas
        val latRounded = String.format("%.2f", lat)
        val lonRounded = String.format("%.2f", lon)
        return "$latRounded-$lonRounded-$radiusKm"
    }

    // ========== INFO Y DEBUG ==========

    /**
     * Obtener estadísticas del caché
     */
    fun getCacheStats(): CacheStats {
        val memorySize = memoryCache.size
        val prefsSize = prefs.all.size / 3 // 3 keys por entrada
        val memoryExpired = memoryCache.count { it.value.isExpired() }

        return CacheStats(
            memoryEntries = memorySize,
            persistentEntries = prefsSize,
            expiredEntries = memoryExpired
        )
    }

    data class CacheStats(
        val memoryEntries: Int,
        val persistentEntries: Int,
        val expiredEntries: Int
    )
}