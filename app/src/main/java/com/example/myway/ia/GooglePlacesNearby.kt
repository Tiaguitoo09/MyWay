package com.example.myway.ai

import android.util.Log
import com.example.myway.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

/**
 * Servicio para buscar lugares usando Google Places Nearby Search (HTTP)
 *
 * COSTO: ~$0.032 por búsqueda (sin Text Search)
 *
 * Ventajas:
 * - Busca en radio configurable (1-50 km)
 * - Encuentra TODOS los lugares (Crepes, Juan Valdez, etc.)
 * - Info actualizada de Google
 *
 * Desventajas:
 * - Cuesta dinero por búsqueda
 * - Requiere conexión a internet
 */
object GooglePlacesNearby {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    private val apiKey = BuildConfig.MAPS_API_KEY

    /**
     * Busca lugares cercanos a una ubicación
     *
     * @param location Ubicación del usuario
     * @param radiusMeters Radio de búsqueda en metros (máx: 50000 = 50km)
     * @param types Tipos de lugares a buscar
     * @return Lista de lugares encontrados
     */
    suspend fun searchNearby(
        location: UserLocation,
        radiusMeters: Int = 5000,
        types: List<String> = listOf("restaurant", "cafe", "park", "museum", "bar")
    ): List<Place> {
        return withContext(Dispatchers.IO) {
            try {
                // Construir URL
                val typeParam = types.joinToString("|")
                val url = "$BASE_URL?" +
                        "location=${location.latitude},${location.longitude}" +
                        "&radius=$radiusMeters" +
                        "&type=${URLEncoder.encode(typeParam, "UTF-8")}" +
                        "&key=$apiKey"

                Log.d("GooglePlaces", "🔍 Buscando en radio de ${radiusMeters}m")

                // Hacer petición HTTP
                val response = URL(url).readText()
                val json = JSONObject(response)

                // Verificar estado
                val status = json.getString("status")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    Log.e("GooglePlaces", "❌ Error: $status")
                    return@withContext emptyList()
                }

                // Parsear resultados
                val results = json.getJSONArray("results")
                val places = mutableListOf<Place>()

                for (i in 0 until results.length()) {
                    val placeJson = results.getJSONObject(i)

                    try {
                        val place = parsePlaceFromJson(placeJson)
                        places.add(place)
                    } catch (e: Exception) {
                        Log.e("GooglePlaces", "Error parseando lugar: ${e.message}")
                    }
                }

                Log.d("GooglePlaces", "✅ Encontrados ${places.size} lugares")

                places

            } catch (e: Exception) {
                Log.e("GooglePlaces", "❌ Error en búsqueda: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * Busca lugares por categoría específica
     */
    suspend fun searchByCategory(
        location: UserLocation,
        category: String,
        radiusMeters: Int = 5000
    ): List<Place> {
        val types = when (category.lowercase()) {
            "restaurante" -> listOf("restaurant", "food")
            "cafe" -> listOf("cafe", "bakery")
            "parque" -> listOf("park")
            "museo" -> listOf("museum", "art_gallery")
            "bar" -> listOf("bar", "night_club")
            "centro_comercial" -> listOf("shopping_mall")
            else -> listOf("restaurant", "cafe", "park")
        }

        return searchNearby(location, radiusMeters, types)
    }

    /**
     * Parsea un lugar del JSON de Google
     */
    private fun parsePlaceFromJson(json: JSONObject): Place {
        val geometry = json.getJSONObject("geometry")
        val locationObj = geometry.getJSONObject("location")

        val lat = locationObj.getDouble("lat")
        val lng = locationObj.getDouble("lng")

        val placeId = json.getString("place_id")
        val name = json.getString("name")
        val address = json.optString("vicinity", "Dirección no disponible")
        val rating = json.optDouble("rating", 0.0)
        val priceLevel = json.optInt("price_level", 2)

        // Obtener tipos
        val typesArray = json.optJSONArray("types")
        val types = if (typesArray != null) {
            (0 until typesArray.length()).map { typesArray.getString(it) }
        } else {
            emptyList()
        }

        // Inferir categoría
        val category = inferCategory(types)

        // Inferir tags
        val tags = inferTags(types, rating, priceLevel)

        return Place(
            id = placeId,
            name = name,
            address = address,
            latitude = lat,
            longitude = lng,
            photoUrl = null, // Se obtiene después si es necesario
            category = category,
            priceLevel = priceLevel,
            rating = rating,
            tags = tags,
            weatherSuitable = getWeatherSuitability(category)
        )
    }

    /**
     * Inferir categoría de los tipos de Google
     */
    private fun inferCategory(types: List<String>): String {
        return when {
            types.any { it == "restaurant" || it == "food" } -> "restaurante"
            types.any { it == "cafe" || it == "bakery" } -> "cafe"
            types.any { it == "park" } -> "parque"
            types.any { it == "museum" || it == "art_gallery" } -> "museo"
            types.any { it == "bar" || it == "night_club" } -> "bar"
            types.any { it == "shopping_mall" } -> "centro_comercial"
            types.any { it == "movie_theater" } -> "cine"
            else -> "otro"
        }
    }

    /**
     * Inferir tags automáticamente según datos de Google
     */
    private fun inferTags(types: List<String>, rating: Double, priceLevel: Int): List<String> {
        val tags = mutableListOf<String>()

        // Por rating
        when {
            rating >= 4.5 -> tags.addAll(listOf("excelente", "recomendado"))
            rating >= 4.0 -> tags.add("bueno")
            rating >= 3.5 -> tags.add("popular")
        }

        // Por precio
        when (priceLevel) {
            0, 1 -> tags.add("económico")
            2 -> tags.add("moderado")
            3, 4 -> tags.addAll(listOf("premium", "elegante"))
        }

        // Por tipo
        types.forEach { type ->
            when (type) {
                "restaurant" -> tags.add("gastronomía")
                "cafe" -> tags.addAll(listOf("acogedor", "tranquilo"))
                "park" -> tags.addAll(listOf("natural", "familiar", "aire libre"))
                "museum" -> tags.addAll(listOf("cultural", "educativo"))
                "bar", "night_club" -> tags.addAll(listOf("social", "nocturno", "vibrante"))
                "shopping_mall" -> tags.addAll(listOf("shopping", "entretenimiento"))
                "romantic_place" -> tags.add("romántico")
            }
        }

        return tags.distinct()
    }

    /**
     * Determinar idoneidad climática según categoría
     */
    private fun getWeatherSuitability(category: String): List<String> {
        return when (category) {
            "parque" -> listOf("soleado", "nublado")
            "museo", "cafe", "centro_comercial", "cine", "bar" -> listOf("soleado", "nublado", "lluvioso")
            "restaurante" -> listOf("soleado", "nublado", "lluvioso")
            else -> listOf("soleado", "nublado", "lluvioso")
        }
    }

    /**
     * Buscar múltiples páginas de resultados (opcional)
     * Google Places retorna máximo 20 lugares por página
     */
    suspend fun searchNearbyWithPagination(
        location: UserLocation,
        radiusMeters: Int = 5000,
        maxResults: Int = 60
    ): List<Place> {
        val allPlaces = mutableListOf<Place>()
        var pageToken: String? = null

        repeat(maxResults / 20) {
            val url = buildUrl(location, radiusMeters, pageToken)

            try {
                val response = URL(url).readText()
                val json = JSONObject(response)

                if (json.getString("status") == "OK") {
                    val results = json.getJSONArray("results")

                    for (i in 0 until results.length()) {
                        val place = parsePlaceFromJson(results.getJSONObject(i))
                        allPlaces.add(place)
                    }

                    pageToken = json.optString("next_page_token", null)

                    if (pageToken.isNullOrEmpty()) {
                        return@repeat
                    }

                    // Google requiere esperar antes de usar next_page_token
                    kotlinx.coroutines.delay(2000)
                } else {
                    return@repeat
                }
            } catch (e: Exception) {
                Log.e("GooglePlaces", "Error en paginación: ${e.message}")
                return@repeat
            }
        }

        return allPlaces
    }

    private fun buildUrl(location: UserLocation, radius: Int, pageToken: String?): String {
        var url = "$BASE_URL?" +
                "location=${location.latitude},${location.longitude}" +
                "&radius=$radius" +
                "&type=restaurant|cafe|park|museum|bar" +
                "&key=$apiKey"

        if (pageToken != null) {
            url += "&pagetoken=$pageToken"
        }

        return url
    }
}