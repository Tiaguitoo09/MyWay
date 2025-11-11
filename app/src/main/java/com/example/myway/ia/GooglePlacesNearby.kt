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

        // ✅ OBTENER FOTO
        val photosArray = json.optJSONArray("photos")
        val photoUrl = if (photosArray != null && photosArray.length() > 0) {
            val photoReference = photosArray.getJSONObject(0).optString("photo_reference")
            buildPhotoUrl(photoReference)
        } else null

        val category = inferCategory(types)
        val tags = inferTags(types, rating, priceLevel)

        return Place(
            id = placeId,
            name = name,
            address = address,
            latitude = lat,
            longitude = lng,
            photoUrl = photoUrl, // ✅ YA NO ES NULL
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

    // ✅ FUNCIÓN PARA CONSTRUIR URL DE FOTO
    private fun buildPhotoUrl(photoReference: String): String {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference=$photoReference&key=$apiKey"
    }

    // ========== BÚSQUEDA OPTIMIZADA PARA RANKING (INCLUYE HOTELES) ==========

    /**
     * Búsqueda OPTIMIZADA para ranking que INCLUYE hoteles
     */
// GooglePlacesNearby.kt - VERSIÓN CON DEBUG COMPLETO

    suspend fun searchNearbyForRanking(
        location: UserLocation,
        radiusMeters: Int = 15000
    ): List<Place> {
        return withContext(Dispatchers.IO) {
            try {
                val rankingTypes = listOf(
                    // Comida y bebida
                    "restaurant", "cafe", "bakery", "food", "meal_takeaway",
                    "bar", "night_club",

                    // Naturaleza y aire libre
                    "park", "campground", "rv_park",

                    // Cultura y entretenimiento
                    "museum", "art_gallery", "aquarium", "zoo",
                    "movie_theater", "bowling_alley", "amusement_park",
                    "tourist_attraction", "point_of_interest",
                    "stadium", "casino",

                    // Teatro y artes escénicas
                    "performing_arts_theater",

                    // Shopping
                    "shopping_mall", "department_store", "store",
                    "clothing_store", "shoe_store", "jewelry_store",
                    "book_store", "electronics_store",

                    // Hoteles
                    "lodging", "hotel"
                )

                val typeParam = rankingTypes.joinToString("|")
                val url = "$BASE_URL?" +
                        "location=${location.latitude},${location.longitude}" +
                        "&radius=$radiusMeters" +
                        "&type=${URLEncoder.encode(typeParam, "UTF-8")}" +
                        "&key=$apiKey"

                Log.d("GooglePlaces", "🔍 Buscando en ${radiusMeters / 1000}km para ranking")

                val response = URL(url).readText()
                val json = JSONObject(response)

                val status = json.getString("status")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    Log.e("GooglePlaces", "❌ API Status: $status")
                    return@withContext emptyList()
                }

                val results = json.getJSONArray("results")
                val places = mutableListOf<Place>()

                // ✅ CONTADOR POR CATEGORÍA PARA DEBUG
                val categoryCount = mutableMapOf<String, Int>()

                for (i in 0 until results.length()) {
                    val placeJson = results.getJSONObject(i)

                    try {
                        val placeName = placeJson.optString("name", "Sin nombre")

                        val typesArray = placeJson.optJSONArray("types")
                        val placeTypes = if (typesArray != null) {
                            (0 until typesArray.length()).map { typesArray.getString(it) }
                        } else {
                            emptyList()
                        }

                        // ✅ LOG DETALLADO DE CADA LUGAR
                        Log.d("GooglePlaces", "📍 [$i] $placeName")
                        Log.d("GooglePlaces", "   Types: ${placeTypes.joinToString(", ")}")

                        if (placeTypes.none { it in rankingTypes }) {
                            Log.d("GooglePlaces", "   ❌ Sin tipos relevantes")
                            continue
                        }

                        val place = parsePlaceFromJsonForRanking(placeJson, placeTypes)

                        // ✅ LOG DE CATEGORÍA ASIGNADA
                        Log.d("GooglePlaces", "   ✅ Categoría: ${place.category}")

                        // ✅ CONTAR POR CATEGORÍA
                        categoryCount[place.category] = categoryCount.getOrDefault(place.category, 0) + 1

                        places.add(place)

                    } catch (e: Exception) {
                        Log.e("GooglePlaces", "Error parseando lugar: ${e.message}")
                    }
                }

                // ✅ LOG RESUMEN POR CATEGORÍA
                Log.d("GooglePlaces", "✅ ${places.size} lugares encontrados para ranking")
                Log.d("GooglePlaces", "📊 DESGLOSE POR CATEGORÍA:")
                categoryCount.forEach { (category, count) ->
                    Log.d("GooglePlaces", "   - $category: $count lugares")
                }

                places

            } catch (e: Exception) {
                Log.e("GooglePlaces", "❌ Error en búsqueda ranking: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun searchBySpecificTypes(
        location: UserLocation,
        radiusMeters: Int = 15000,
        types: List<String>
    ): List<Place> {
        return withContext(Dispatchers.IO) {
            try {
                val typeParam = types.joinToString("|")
                val url = "$BASE_URL?" +
                        "location=${location.latitude},${location.longitude}" +
                        "&radius=$radiusMeters" +
                        "&type=${URLEncoder.encode(typeParam, "UTF-8")}" +
                        "&key=$apiKey"

                Log.d("GooglePlaces", "🔍 Búsqueda específica de tipos: ${types.joinToString(", ")}")

                val response = URL(url).readText()
                val json = JSONObject(response)

                val status = json.getString("status")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    Log.e("GooglePlaces", "❌ API Status: $status")
                    return@withContext emptyList()
                }

                val results = json.getJSONArray("results")
                val places = mutableListOf<Place>()

                Log.d("GooglePlaces", "📦 API devolvió ${results.length()} resultados")

                for (i in 0 until results.length()) {
                    val placeJson = results.getJSONObject(i)

                    try {
                        val placeName = placeJson.optString("name", "Sin nombre")

                        val typesArray = placeJson.optJSONArray("types")
                        val placeTypes = if (typesArray != null) {
                            (0 until typesArray.length()).map { typesArray.getString(it) }
                        } else {
                            emptyList()
                        }

                        // ✅ Verificar que tenga al menos uno de los tipos solicitados
                        if (placeTypes.none { it in types }) {
                            Log.d("GooglePlaces", "❌ [$i] $placeName: sin tipos relevantes")
                            continue
                        }

                        // ✅ Parsear usando la función para ranking
                        val place = parsePlaceFromJsonForRanking(placeJson, placeTypes)

                        Log.d("GooglePlaces", "✅ [$i] $placeName -> ${place.category} (⭐${place.rating})")

                        places.add(place)

                    } catch (e: Exception) {
                        Log.e("GooglePlaces", "Error parseando lugar: ${e.message}")
                    }
                }

                Log.d("GooglePlaces", "✅ ${places.size} lugares válidos de ${types.size} tipos")
                places

            } catch (e: Exception) {
                Log.e("GooglePlaces", "❌ Error en búsqueda específica: ${e.message}", e)
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

        val photosArray = json.optJSONArray("photos")
        val photoUrl = if (photosArray != null && photosArray.length() > 0) {
            val photoReference = photosArray.getJSONObject(0).optString("photo_reference")
            buildPhotoUrl(photoReference)
        } else null

        val category = inferCategoryForRanking(types)
        val tags = inferTagsForRanking(types, rating, priceLevel, category)

        return Place(
            id = placeId,
            name = name,
            address = address,
            latitude = lat,
            longitude = lng,
            photoUrl = photoUrl,
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
        Log.d("GooglePlaces", "      Analizando tipos: ${types.joinToString(", ")}")

        val category = when {
            // ✅ PRIORIDAD 1: VIDA NOCTURNA (debe ir ANTES que hoteles)
            types.any { it == "night_club" } -> "discoteca"
            types.any { it == "bar" && "night_club" !in types } -> "bar"

            // ✅ PRIORIDAD 2: Comida y bebida
            types.any { it == "cafe" || it == "bakery" } && "restaurant" !in types -> "cafe"
            types.any { it == "restaurant" || it == "food" || it == "meal_takeaway" } -> "restaurante"

            // ✅ PRIORIDAD 3: Naturaleza
            types.any { it == "park" || it == "campground" || it == "rv_park" } -> "parque"

            // ✅ PRIORIDAD 4: Cultura y entretenimiento
            types.any { it == "museum" || it == "art_gallery" || it == "aquarium" || it == "zoo" } -> "museo"
            types.any { it == "performing_arts_theater" } -> "teatro"
            types.any { it == "movie_theater" } -> "cine"
            types.any { it == "bowling_alley" || it == "amusement_park" || it == "casino" } -> "entretenimiento"
            types.any { it == "stadium" } -> "entretenimiento"

            // ✅ PRIORIDAD 5: Shopping
            types.any { it == "shopping_mall" || it == "department_store" } -> "centro_comercial"
            types.any { it == "store" || it == "clothing_store" || it == "shoe_store" ||
                    it == "jewelry_store" || it == "book_store" || it == "electronics_store" } -> "zona_comercial"

            // ✅ PRIORIDAD 6: Hoteles (AL FINAL para evitar conflictos)
            types.any { it == "lodging" || it == "hotel" } &&
                    types.none { it in listOf("bar", "night_club", "restaurant", "cafe") } -> "hotel"

            // ✅ PRIORIDAD 7: Turismo (SOLO si no es otra categoría)
            types.any { it == "tourist_attraction" } &&
                    types.none { it in listOf("restaurant", "cafe", "bar", "museum", "park", "shopping_mall", "lodging", "night_club") } ->
                "atraccion_turistica"

            // Punto de interés genérico
            types.any { it == "point_of_interest" } &&
                    types.none { it in listOf("restaurant", "cafe", "bar", "museum", "park", "shopping_mall", "lodging", "night_club") } ->
                "atraccion_turistica"

            else -> {
                Log.d("GooglePlaces", "      ⚠️ Sin categoría definida -> otro")
                "otro"
            }
        }

        Log.d("GooglePlaces", "      → Categoría asignada: $category")
        return category
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
            rating >= 4.7 -> tags.addAll(listOf("excelente", "recomendado", "popular", "imperdible"))
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

        // Tags por categoría
        when (category) {
            "hotel" -> tags.addAll(listOf("alojamiento", "hospedaje"))
            "restaurante" -> tags.addAll(listOf("gastronomía", "comida"))
            "cafe" -> tags.addAll(listOf("acogedor", "tranquilo", "café"))
            "parque" -> tags.addAll(listOf("natural", "familiar", "aire libre", "recreación"))
            "museo" -> tags.addAll(listOf("cultural", "educativo", "arte"))
            "teatro" -> tags.addAll(listOf("cultural", "espectáculos", "arte", "entretenimiento"))
            "cine" -> tags.addAll(listOf("entretenimiento", "películas"))
            "bar", "discoteca" -> tags.addAll(listOf("social", "nocturno", "vibrante", "fiesta"))
            "centro_comercial" -> tags.addAll(listOf("shopping", "entretenimiento", "compras"))
            "zona_comercial" -> tags.addAll(listOf("tiendas", "compras"))
            "atraccion_turistica" -> tags.addAll(listOf("turístico", "imperdible", "visita obligada"))
            "entretenimiento" -> tags.addAll(listOf("diversión", "ocio", "actividades"))
        }

        // Tags por tipos específicos
        if ("tourist_attraction" in types) tags.add("turístico")
        if ("point_of_interest" in types) tags.add("destacado")
        if ("zoo" in types || "aquarium" in types) tags.addAll(listOf("familiar", "animales"))
        if ("stadium" in types) tags.addAll(listOf("deportes", "eventos"))
        if ("casino" in types) tags.addAll(listOf("juegos", "adultos"))

        return tags.distinct()
    }
}