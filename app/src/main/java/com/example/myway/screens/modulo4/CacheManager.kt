package com.example.myway.data

import android.content.Context
import android.util.Log
import com.example.myway.ai.Place
import com.example.myway.screens.modulo4.PlaceCategory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.myway.ai.toMap
import java.util.concurrent.TimeUnit

class CacheManager(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("places_cache", Context.MODE_PRIVATE)

    // ✅ Cache en memoria
    private val memoryCache = mutableMapOf<String, CacheEntry>()

    // ✅ Cache específico para rankings
    private val rankingCache = mutableMapOf<String, RankingCacheEntry>()

    companion object {
        private const val TAG = "CacheManager"

        // Duraciones de caché
        private val FIREBASE_CACHE_DURATION = TimeUnit.HOURS.toMillis(24) // 24 horas
        private val GOOGLE_PLACES_CACHE_DURATION = TimeUnit.MINUTES.toMillis(30) // 30 min
        private val MEMORY_CACHE_DURATION = TimeUnit.MINUTES.toMillis(10) // 10 min
        private val RANKING_CACHE_DURATION = TimeUnit.MINUTES.toMillis(15) // 15 min

        // Claves de caché
        const val KEY_FIREBASE_PLACES = "firebase_places"
        const val KEY_GOOGLE_NEARBY = "google_nearby"
    }

    // ========== DATA CLASSES ==========

    private data class CacheEntry(
        val places: List<Place>,
        val timestamp: Long,
        val expiresDuration: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > expiresDuration
        }
    }

    private data class RankingCacheEntry(
        val places: List<Place>,
        val timestamp: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > RANKING_CACHE_DURATION
        }
    }

    // ========== CACHÉ DE LUGARES DE FIREBASE (24h) ==========

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

            Log.d(TAG, "💾 ${places.size} lugares de Firebase en caché")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando caché Firebase: ${e.message}")
        }
    }

    fun getFirebasePlacesCache(): List<Place>? {
        // 1. Intentar memoria primero
        memoryCache[KEY_FIREBASE_PLACES]?.let { entry ->
            if (!entry.isExpired()) {
                Log.d(TAG, "✅ Cache hit (memoria): Firebase places")
                return entry.places
            }
        }

        // 2. Intentar SharedPreferences
        return getFromPrefs(KEY_FIREBASE_PLACES, FIREBASE_CACHE_DURATION)?.also {
            Log.d(TAG, "✅ Cache hit (prefs): Firebase places")
            // Restaurar a memoria
            memoryCache[KEY_FIREBASE_PLACES] = CacheEntry(
                places = it,
                timestamp = System.currentTimeMillis(),
                expiresDuration = FIREBASE_CACHE_DURATION
            )
        }
    }

    // ========== CACHÉ DE GOOGLE PLACES (30 min) ==========

    fun cacheGooglePlaces(locationKey: String, places: List<Place>) {
        try {
            val key = "$KEY_GOOGLE_NEARBY-$locationKey"

            // Solo caché en memoria (no persistir búsquedas)
            memoryCache[key] = CacheEntry(
                places = places,
                timestamp = System.currentTimeMillis(),
                expiresDuration = GOOGLE_PLACES_CACHE_DURATION
            )

            Log.d(TAG, "💾 ${places.size} lugares de Google en caché (memoria)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando caché Google: ${e.message}")
        }
    }

    fun getGooglePlacesCache(locationKey: String): List<Place>? {
        val key = "$KEY_GOOGLE_NEARBY-$locationKey"

        return memoryCache[key]?.let { entry ->
            if (!entry.isExpired()) {
                Log.d(TAG, "✅ Cache hit (memoria): Google places [$locationKey]")
                entry.places
            } else {
                Log.d(TAG, "⏰ Cache expirado: Google places [$locationKey]")
                memoryCache.remove(key)
                null
            }
        }
    }

    // ========== NUEVO: CACHÉ ESPECÍFICO PARA RANKINGS (15 min) ==========

    /**
     * Guardar ranking en cache
     */
    fun cacheRankingPlaces(
        category: PlaceCategory,
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
        places: List<Place>
    ) {
        try {
            val key = generateRankingCacheKey(category, latitude, longitude, radiusKm)

            rankingCache[key] = RankingCacheEntry(
                places = places,
                timestamp = System.currentTimeMillis()
            )

            Log.d(TAG, "💾 ${places.size} lugares de ${category.displayName} en caché de ranking")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando caché de ranking: ${e.message}")
        }
    }

    /**
     * Obtener ranking del cache
     */
    fun getRankingCache(
        category: PlaceCategory,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Place>? {
        val key = generateRankingCacheKey(category, latitude, longitude, radiusKm)

        return rankingCache[key]?.let { entry ->
            if (!entry.isExpired()) {
                Log.d(TAG, "✅ Cache hit (ranking): ${category.displayName}")
                entry.places
            } else {
                Log.d(TAG, "⏰ Cache expirado (ranking): ${category.displayName}")
                rankingCache.remove(key)
                null
            }
        }
    }

    /**
     * Generar clave única para ranking
     */
    private fun generateRankingCacheKey(
        category: PlaceCategory,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): String {
        val latRounded = (latitude * 100).toInt() / 100.0
        val lngRounded = (longitude * 100).toInt() / 100.0
        return "ranking_${category.name}_${latRounded}_${lngRounded}_${radiusKm.toInt()}"
    }

    /**
     * Limpiar cache de rankings
     */
    fun clearRankingCache() {
        rankingCache.clear()
        Log.d(TAG, "🧹 Cache de rankings limpiado")
    }

    /**
     * Limpiar rankings expirados
     */
    fun cleanExpiredRankingCache() {
        val toRemove = rankingCache.filter { it.value.isExpired() }.keys
        toRemove.forEach { rankingCache.remove(it) }

        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "🧹 ${toRemove.size} rankings expirados eliminados")
        }
    }

    // ========== HELPERS PRIVADOS ==========

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

    private fun clearPrefsKey(key: String) {
        prefs.edit()
            .remove("$key-data")
            .remove("$key-timestamp")
            .remove("$key-duration")
            .apply()
    }

    // ========== SERIALIZACIÓN JSON ==========

    private fun placesToJson(places: List<Place>): String {
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

    fun clearMemoryCache() {
        memoryCache.clear()
        Log.d(TAG, "🧹 Caché en memoria limpiado")
    }

    fun clearAllCache() {
        memoryCache.clear()
        rankingCache.clear()
        prefs.edit().clear().apply()
        Log.d(TAG, "🧹 Todo el caché limpiado")
    }

    fun cleanExpiredCache() {
        // Limpiar memoria
        val expiredKeys = memoryCache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { memoryCache.remove(it) }

        if (expiredKeys.isNotEmpty()) {
            Log.d(TAG, "🧹 Limpiados ${expiredKeys.size} items expirados de memoria")
        }

        // Limpiar rankings expirados
        cleanExpiredRankingCache()
    }

    fun generateLocationKey(lat: Double, lon: Double, radiusKm: Double): String {
        // Redondear a 2 decimales para agrupar búsquedas cercanas
        val latRounded = String.format("%.2f", lat)
        val lonRounded = String.format("%.2f", lon)
        return "$latRounded-$lonRounded-$radiusKm"
    }

    // ========== INFO Y DEBUG ==========

    fun getCacheStats(): CacheStats {
        val memorySize = memoryCache.size
        val rankingSize = rankingCache.size
        val prefsSize = prefs.all.size / 3 // 3 keys por entrada
        val memoryExpired = memoryCache.count { it.value.isExpired() }
        val rankingExpired = rankingCache.count { it.value.isExpired() }

        return CacheStats(
            memoryEntries = memorySize,
            rankingEntries = rankingSize,
            persistentEntries = prefsSize,
            expiredEntries = memoryExpired + rankingExpired
        )
    }

    data class CacheStats(
        val memoryEntries: Int,
        val rankingEntries: Int,
        val persistentEntries: Int,
        val expiredEntries: Int
    )
}