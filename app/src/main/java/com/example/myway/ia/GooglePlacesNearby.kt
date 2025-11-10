package com.example.myway.ai

import android.util.Log
import com.example.myway.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

object GooglePlacesNearby {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    private val apiKey = BuildConfig.MAPS_API_KEY

    //Lista blanca estricta
    private val ALLOWED_TYPES = setOf(
        "restaurant", "cafe", "bakery", "food",
        "bar", "night_club",
        "park", "tourist_attraction",
        "museum", "art_gallery", "aquarium",
        "shopping_mall", "store",
        "movie_theater", "bowling_alley", "amusement_park"
    )

    //Lista negra
    private val BLOCKED_TYPES = setOf(
        "lodging", "hotel", "motel", "hostel", "campground",
        "car_rental", "gas_station", "parking",
        "hospital", "pharmacy", "doctor",
        "bank", "atm", "post_office",
        "school", "university", "library",
        "church", "mosque", "synagogue", "hindu_temple",
        "cemetery", "funeral_home",
        "airport", "train_station", "bus_station", "subway_station",
        "car_dealer", "car_repair", "car_wash",
        "dentist", "veterinary_care", "pet_store",
        "laundry", "hair_care", "beauty_salon", "spa",
        "gym", "physiotherapist",
        "insurance_agency", "real_estate_agency", "moving_company",
        "storage", "locksmith", "plumber", "electrician", "roofing_contractor"
    )

    suspend fun searchNearby(
        location: UserLocation,
        radiusMeters: Int = 5000,
        types: List<String> = listOf("restaurant", "cafe", "park", "museum", "bar")
    ): List<Place> {
        return withContext(Dispatchers.IO) {
            try {
                val typeParam = types.joinToString("|")
                val url = "$BASE_URL?" +
                        "location=${location.latitude},${location.longitude}" +
                        "&radius=$radiusMeters" +
                        "&type=${URLEncoder.encode(typeParam, "UTF-8")}" +
                        "&key=$apiKey"

                Log.d("GooglePlaces", "🔍 Buscando en radio de ${radiusMeters}m")

                val response = URL(url).readText()
                val json = JSONObject(response)

                val status = json.getString("status")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    Log.e("GooglePlaces", "❌ Error: $status")
                    return@withContext emptyList()
                }

                val results = json.getJSONArray("results")
                val places = mutableListOf<Place>()

                for (i in 0 until results.length()) {
                    val placeJson = results.getJSONObject(i)

                    try {
                        //Verificar tipos antes de parsear
                        val typesArray = placeJson.optJSONArray("types")
                        val placeTypes = if (typesArray != null) {
                            (0 until typesArray.length()).map { typesArray.getString(it) }
                        } else {
                            emptyList()
                        }

                        //Rechazar si contiene tipos prohibidos
                        if (placeTypes.any { it in BLOCKED_TYPES }) {
                            Log.d("GooglePlaces", "❌ Rechazado (tipo prohibido): ${placeJson.optString("name")}")
                            continue
                        }

                        // Solo aceptar si tiene al menos un tipo permitido
                        if (placeTypes.none { it in ALLOWED_TYPES }) {
                            Log.d("GooglePlaces", "❌ Rechazado (sin tipos válidos): ${placeJson.optString("name")}")
                            continue
                        }

                        val place = parsePlaceFromJson(placeJson)

                        if (place.category in listOf("hotel", "hospedaje", "otro")) {
                            Log.d("GooglePlaces", "❌ Rechazado por categoría: ${place.name} (${place.category})")
                            continue
                        }

                        places.add(place)

                    } catch (e: Exception) {
                        Log.e("GooglePlaces", "Error parseando lugar: ${e.message}")
                    }
                }

                Log.d("GooglePlaces", "✅ ${places.size} lugares válidos encontrados")
                places

            } catch (e: Exception) {
                Log.e("GooglePlaces", "❌ Error en búsqueda: ${e.message}", e)
                emptyList()
            }
        }
    }

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

        val typesArray = json.optJSONArray("types")
        val types = if (typesArray != null) {
            (0 until typesArray.length()).map { typesArray.getString(it) }
        } else {
            emptyList()
        }

        val category = inferCategory(types)
        val tags = inferTags(types, rating, priceLevel)

        return Place(
            id = placeId,
            name = name,
            address = address,
            latitude = lat,
            longitude = lng,
            photoUrl = null,
            category = category,
            priceLevel = priceLevel,
            rating = rating,
            tags = tags,
            weatherSuitable = getWeatherSuitability(category)
        )
    }

    private fun inferCategory(types: List<String>): String {
        // ❌ Primero rechazar tipos no deseados
        if (types.any { it in BLOCKED_TYPES }) {
            return "otro" // Se filtrará después
        }

        return when {
            types.any { it == "night_club" } -> "discoteca"
            types.any { it == "bar" } -> "bar"
            types.any { it == "restaurant" || it == "food" } -> "restaurante"
            types.any { it == "cafe" || it == "bakery" } -> "cafe"
            types.any { it == "park" } -> "parque"
            types.any { it == "museum" || it == "art_gallery" || it == "aquarium" } -> "museo"
            types.any { it == "shopping_mall" } -> "centro_comercial"
            types.any { it == "movie_theater" } -> "cine"
            types.any { it == "tourist_attraction" } -> "atraccion_turistica"
            types.any { it == "bowling_alley" || it == "amusement_park" } -> "entretenimiento"
            else -> "otro"
        }
    }

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
                "bar" -> tags.addAll(listOf("social", "nocturno", "vibrante", "fin de semana"))
                "night_club" -> tags.addAll(listOf("social", "nocturno", "vibrante", "fin de semana"))
                "shopping_mall" -> tags.addAll(listOf("shopping", "entretenimiento"))
                "tourist_attraction" -> tags.add("turístico")
            }
        }

        return tags.distinct()
    }

    private fun getWeatherSuitability(category: String): List<String> {
        return when (category) {
            "parque" -> listOf("soleado", "nublado")
            "museo", "cafe", "centro_comercial", "cine", "bar", "discoteca" ->
                listOf("soleado", "nublado", "lluvioso")
            "restaurante" -> listOf("soleado", "nublado", "lluvioso")
            else -> listOf("soleado", "nublado", "lluvioso")
        }
    }



    // ========== BÚSQUEDA OPTIMIZADA PARA RANKING (INCLUYE HOTELES) ==========

    /**
     * Búsqueda OPTIMIZADA para ranking que INCLUYE hoteles
     */
    suspend fun searchNearbyForRanking(
        location: UserLocation,
        radiusMeters: Int = 10000
    ): List<Place> {
        return withContext(Dispatchers.IO) {
            try {
                // Tipos permitidos para ranking (incluye hoteles)
                val rankingTypes = listOf(
                    "restaurant", "cafe", "bakery", "food",
                    "bar", "night_club",
                    "park", "tourist_attraction",
                    "museum", "art_gallery", "aquarium",
                    "shopping_mall", "store",
                    "movie_theater", "bowling_alley", "amusement_park",
                    "lodging" // ⭐ HOTELES PERMITIDOS PARA RANKING
                )

                val typeParam = rankingTypes.joinToString("|")
                val url = "$BASE_URL?" +
                        "location=${location.latitude},${location.longitude}" +
                        "&radius=$radiusMeters" +
                        "&type=${URLEncoder.encode(typeParam, "UTF-8")}" +
                        "&key=$apiKey"

                Log.d("GooglePlaces", "🔍 Ranking: ${radiusMeters}m")

                val response = URL(url).readText()
                val json = JSONObject(response)

                val status = json.getString("status")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    Log.e("GooglePlaces", "❌ Status: $status")
                    return@withContext emptyList()
                }

                val results = json.getJSONArray("results")
                val places = mutableListOf<Place>()

                for (i in 0 until results.length()) {
                    val placeJson = results.getJSONObject(i)

                    try {
                        val typesArray = placeJson.optJSONArray("types")
                        val placeTypes = if (typesArray != null) {
                            (0 until typesArray.length()).map { typesArray.getString(it) }
                        } else {
                            emptyList()
                        }

                        // Verificar que tenga tipos relevantes
                        if (placeTypes.none { it in rankingTypes }) {
                            continue
                        }

                        // Parsear con categoría correcta para ranking
                        val place = parsePlaceFromJsonForRanking(placeJson, placeTypes)
                        places.add(place)

                    } catch (e: Exception) {
                        Log.e("GooglePlaces", "Error parseando: ${e.message}")
                    }
                }

                Log.d("GooglePlaces", "✅ ${places.size} lugares (ranking)")
                places

            } catch (e: Exception) {
                Log.e("GooglePlaces", "❌ Error ranking: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * Parser ESPECÍFICO para ranking que detecta hoteles correctamente
     */
    private fun parsePlaceFromJsonForRanking(json: JSONObject, types: List<String>): Place {
        val geometry = json.getJSONObject("geometry")
        val locationObj = geometry.getJSONObject("location")

        val lat = locationObj.getDouble("lat")
        val lng = locationObj.getDouble("lng")

        val placeId = json.getString("place_id")
        val name = json.getString("name")
        val address = json.optString("vicinity", "Dirección no disponible")
        val rating = json.optDouble("rating", 0.0)
        val priceLevel = json.optInt("price_level", 2)

        // Usar función específica para ranking que detecta hoteles
        val category = inferCategoryForRanking(types)
        val tags = inferTagsForRanking(types, rating, priceLevel, category)

        return Place(
            id = placeId,
            name = name,
            address = address,
            latitude = lat,
            longitude = lng,
            photoUrl = null,
            category = category,
            priceLevel = priceLevel,
            rating = rating,
            tags = tags,
            weatherSuitable = getWeatherSuitability(category)
        )
    }

    /**
     * Detectar categoría INCLUYENDO hoteles (para ranking)
     */
    private fun inferCategoryForRanking(types: List<String>): String {
        return when {
            // ⭐ HOTELES (ahora sí se detectan)
            types.any { it in listOf("lodging", "hotel", "motel", "hostel") } -> "hotel"

            // Resto de categorías (con mejor detección)
            types.any { it == "night_club" } -> "discoteca"
            types.any { it == "bar" } -> "bar"
            types.any { it == "restaurant" || it == "food" } -> "restaurante"
            types.any { it == "cafe" || it == "bakery" } -> "cafe"
            types.any { it == "park" } -> "parque"
            types.any { it == "museum" || it == "art_gallery" || it == "aquarium" } -> "museo"
            types.any { it == "shopping_mall" || it == "shopping_center" } -> "centro_comercial"
            types.any { it == "movie_theater" } -> "cine"
            types.any { it == "tourist_attraction" } -> "atraccion_turistica"
            types.any { it == "bowling_alley" || it == "amusement_park" } -> "entretenimiento"
            types.any { it == "store" || it == "clothing_store" } -> "zona_comercial"
            else -> "otro"
        }
    }

    /**
     * Tags mejorados para ranking
     */
    private fun inferTagsForRanking(
        types: List<String>,
        rating: Double,
        priceLevel: Int,
        category: String
    ): List<String> {
        val tags = mutableListOf<String>()

        // Rating
        when {
            rating >= 4.5 -> tags.addAll(listOf("excelente", "recomendado", "popular"))
            rating >= 4.0 -> tags.addAll(listOf("bueno", "recomendado"))
            rating >= 3.5 -> tags.add("popular")
        }

        // Precio
        when (priceLevel) {
            0, 1 -> tags.add("económico")
            2 -> tags.add("moderado")
            3, 4 -> tags.addAll(listOf("premium", "elegante"))
        }

        // Por categoría específica
        when (category) {
            "hotel" -> tags.addAll(listOf("alojamiento", "hospedaje"))
            "restaurante" -> tags.add("gastronomía")
            "cafe" -> tags.addAll(listOf("acogedor", "tranquilo"))
            "parque" -> tags.addAll(listOf("natural", "familiar", "aire libre"))
            "museo" -> tags.addAll(listOf("cultural", "educativo"))
            "bar", "discoteca" -> tags.addAll(listOf("social", "nocturno", "vibrante"))
            "centro_comercial" -> tags.addAll(listOf("shopping", "entretenimiento"))
            "atraccion_turistica" -> tags.addAll(listOf("turístico", "imperdible"))
        }

        return tags.distinct()
    }
}