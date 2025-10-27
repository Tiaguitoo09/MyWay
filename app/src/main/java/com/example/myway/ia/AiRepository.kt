package com.example.myway.ai

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place as GooglePlace
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class AIRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val weights = ScoringWeights()

    // ========== RECOMENDACIÓN RÁPIDA ==========

    suspend fun getQuickRecommendation(
        request: QuickRecommendationRequest
    ): Result<AIRecommendation> {
        return try {
            // 1. Obtener contexto del usuario
            val userContext = getUserContext(request.userId)

            // 2. Buscar lugares cercanos (Firebase + Google Places)
            val nearbyPlaces = searchNearbyPlaces(
                request.userLocation,
                radiusKm = 10.0
            )

            if (nearbyPlaces.isEmpty()) {
                return Result.failure(Exception("No se encontraron lugares cercanos"))
            }

            Log.d("AIRepository", "📍 Encontrados ${nearbyPlaces.size} lugares")

            // 3. Filtrar por contexto
            val filteredPlaces = filterByContext(
                places = nearbyPlaces,
                timeOfDay = request.timeOfDay,
                weather = request.currentWeather
            )

            Log.d("AIRepository", "✅ Filtrados: ${filteredPlaces.size} lugares")

            // 4. Calcular score
            val scoredPlaces = filteredPlaces.map { place ->
                val score = calculateQuickScore(
                    place = place,
                    userLocation = request.userLocation,
                    timeOfDay = request.timeOfDay,
                    weather = request.currentWeather,
                    userContext = userContext
                )

                val distance = calculateDistance(
                    request.userLocation.latitude,
                    request.userLocation.longitude,
                    place.latitude,
                    place.longitude
                )

                AIRecommendation(
                    place = place,
                    score = score,
                    reason = generateQuickReason(place, request.timeOfDay, request.currentWeather),
                    distance = distance,
                    estimatedDuration = estimateDuration(distance)
                )
            }

            // 5. Seleccionar el mejor
            val topRecommendations = scoredPlaces
                .sortedByDescending { it.score }
                .take(5)

            if (topRecommendations.isEmpty()) {
                return Result.failure(Exception("No se encontró una buena recomendación"))
            }

            val selected = if (topRecommendations.size > 1) {
                if (Random.nextDouble() < 0.7) {
                    topRecommendations.first()
                } else {
                    topRecommendations.drop(1).randomOrNull() ?: topRecommendations.first()
                }
            } else {
                topRecommendations.first()
            }

            Log.d("AIRepository", "🏆 Seleccionado: ${selected.place.name} (Score: ${selected.score})")

            Result.success(selected)

        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== RECOMENDACIÓN PERSONALIZADA ==========

    suspend fun getPersonalizedRecommendation(
        request: PersonalizedRecommendationRequest
    ): Result<AIRecommendation> {
        return try {
            val userContext = getUserContext(request.userId)

            val nearbyPlaces = searchNearbyPlaces(
                request.userLocation,
                radiusKm = 15.0
            )

            if (nearbyPlaces.isEmpty()) {
                return Result.failure(Exception("No se encontraron lugares cercanos"))
            }

            val filteredPlaces = filterByPersonalizedCriteria(
                places = nearbyPlaces,
                mood = request.mood,
                planType = request.planType,
                budget = request.budget,
                duration = request.duration,
                timeOfDay = request.timeOfDay,
                weather = request.currentWeather
            )

            val scoredPlaces = filteredPlaces.map { place ->
                val score = calculatePersonalizedScore(
                    place = place,
                    request = request,
                    userContext = userContext
                )

                val distance = calculateDistance(
                    request.userLocation.latitude,
                    request.userLocation.longitude,
                    place.latitude,
                    place.longitude
                )

                AIRecommendation(
                    place = place,
                    score = score,
                    reason = generatePersonalizedReason(place, request),
                    distance = distance,
                    estimatedDuration = estimateDuration(distance)
                )
            }

            val best = scoredPlaces
                .sortedByDescending { it.score }
                .firstOrNull()
                ?: return Result.failure(Exception("No se encontró ninguna recomendación"))

            Result.success(best)

        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== BÚSQUEDA DE LUGARES (HÍBRIDO) ==========

    private suspend fun searchNearbyPlaces(
        location: UserLocation,
        radiusKm: Double
    ): List<Place> {
        val allPlaces = mutableListOf<Place>()

        // 1. Buscar en Firebase primero (tus lugares curados)
        val firebasePlaces = getPlacesFromFirebase(location, radiusKm)
        allPlaces.addAll(firebasePlaces)

        Log.d("AIRepository", "📦 Firebase: ${firebasePlaces.size} lugares")

        // 2. Buscar en Google Places (lugares reales)
        val googlePlaces = searchGooglePlaces(location)
        allPlaces.addAll(googlePlaces)

        Log.d("AIRepository", "🌍 Google Places: ${googlePlaces.size} lugares")

        // 3. Eliminar duplicados por nombre
        return allPlaces.distinctBy { it.name.lowercase() }
    }

    // Buscar en Firebase (tus 20 lugares curados)
    private suspend fun getPlacesFromFirebase(
        location: UserLocation,
        radiusKm: Double
    ): List<Place> {
        return try {
            val snapshot = firestore.collection("lugares")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Place(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        address = doc.getString("address") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        photoUrl = doc.getString("photoUrl"),
                        category = doc.getString("category") ?: "otro",
                        priceLevel = doc.getLong("priceLevel")?.toInt() ?: 2,
                        rating = doc.getDouble("rating") ?: 4.0,
                        tags = (doc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        weatherSuitable = (doc.get("weatherSuitable") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    )
                } catch (e: Exception) {
                    Log.e("AIRepository", "Error parseando lugar: ${e.message}")
                    null
                }
            }.filter { place ->
                val distance = calculateDistance(
                    location.latitude, location.longitude,
                    place.latitude, place.longitude
                )
                distance <= radiusKm
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Error obteniendo de Firebase: ${e.message}")
            emptyList()
        }
    }

    // Buscar en Google Places API usando HTTP
    private suspend fun searchGooglePlaces(location: UserLocation): List<Place> {
        return try {
            // Usar el servicio HTTP de búsqueda cercana
            GooglePlacesNearby.searchNearby(
                location = location,
                radiusMeters = 5000, // 5 km
                types = listOf("restaurant", "cafe", "park", "museum", "bar", "shopping_mall")
            )
        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error buscando en Google Places: ${e.message}")
            emptyList()
        }
    }

    // Inferir categoría de tipos de Google
    private fun inferCategoryFromTypes(types: List<GooglePlace.Type>?): String {
        if (types == null) return "otro"

        return when {
            types.any { it.name.contains("restaurant", true) } -> "restaurante"
            types.any { it.name.contains("cafe", true) } -> "cafe"
            types.any { it.name.contains("park", true) } -> "parque"
            types.any { it.name.contains("museum", true) } -> "museo"
            types.any { it.name.contains("bar", true) || it.name.contains("night_club", true) } -> "bar"
            types.any { it.name.contains("shopping", true) || it.name.contains("mall", true) } -> "centro_comercial"
            types.any { it.name.contains("movie", true) || it.name.contains("theater", true) } -> "cine"
            else -> "otro"
        }
    }

    // Inferir tags automáticamente de datos de Google
    private fun inferTagsFromGoogle(googlePlace: GooglePlace): List<String> {
        val tags = mutableListOf<String>()

        // Por rating
        val rating = googlePlace.rating ?: 0.0
        when {
            rating >= 4.5 -> tags.addAll(listOf("excelente", "recomendado"))
            rating >= 4.0 -> tags.add("bueno")
        }

        // Por precio
        when (googlePlace.priceLevel) {
            0, 1 -> tags.add("económico")
            2 -> tags.add("moderado")
            3, 4 -> tags.addAll(listOf("premium", "elegante"))
        }

        // Por tipo
        googlePlace.types?.forEach { type ->
            when {
                type.name.contains("restaurant", true) -> tags.add("gastronomía")
                type.name.contains("cafe", true) -> tags.addAll(listOf("acogedor", "café"))
                type.name.contains("park", true) -> tags.addAll(listOf("natural", "familiar", "aire libre"))
                type.name.contains("museum", true) -> tags.addAll(listOf("cultural", "educativo"))
                type.name.contains("bar", true) -> tags.addAll(listOf("social", "nocturno"))
                type.name.contains("romantic", true) -> tags.add("romántico")
            }
        }

        // Por popularidad
        val ratingsTotal = googlePlace.userRatingsTotal ?: 0
        if (ratingsTotal > 1000) {
            tags.add("popular")
        }

        return tags.distinct()
    }

    // Idoneidad climática según categoría
    private fun getWeatherSuitability(category: String): List<String> {
        return when (category) {
            "parque", "mirador" -> listOf("soleado", "nublado")
            "museo", "cafe", "centro_comercial", "cine" -> listOf("soleado", "nublado", "lluvioso")
            else -> listOf("soleado", "nublado", "lluvioso")
        }
    }

    // ========== CÁLCULO DE SCORES (sin cambios) ==========

    private fun calculateQuickScore(
        place: Place,
        userLocation: UserLocation,
        timeOfDay: String,
        weather: String?,
        userContext: UserContext
    ): Double {
        var score = 0.0

        val timeMatch = when (timeOfDay.lowercase()) {
            "mañana" -> if (place.category in listOf("cafe", "parque", "museo")) 1.0 else 0.5
            "tarde" -> if (place.category in listOf("restaurante", "parque")) 1.0 else 0.6
            "noche" -> if (place.category in listOf("bar", "restaurante", "teatro")) 1.0 else 0.4
            else -> 0.5
        }

        val weatherMatch = if (weather != null) {
            when (weather.lowercase()) {
                "soleado" -> if (place.category in listOf("parque", "mirador")) 1.0 else 0.7
                "lluvioso" -> if (place.category in listOf("museo", "cafe", "cine")) 1.0 else 0.3
                else -> 0.7
            }
        } else 0.7

        score += (timeMatch * 0.25 + weatherMatch * 0.15) * 100

        val categoryMatch = if (place.category in userContext.favoriteCategories) 1.0 else 0.5
        score += categoryMatch * 30

        score += (place.rating / 5.0) * 20

        val distance = calculateDistance(
            userLocation.latitude, userLocation.longitude,
            place.latitude, place.longitude
        )
        val distanceScore = when {
            distance < 2.0 -> 1.0
            distance < 5.0 -> 0.8
            distance < 10.0 -> 0.5
            else -> 0.2
        }
        score += distanceScore * 10

        return score.coerceIn(0.0, 100.0)
    }

    private fun calculatePersonalizedScore(
        place: Place,
        request: PersonalizedRecommendationRequest,
        userContext: UserContext
    ): Double {
        var score = 0.0

        val moodTags = when (request.mood.lowercase()) {
            "feliz" -> listOf("vibrante", "social", "energético")
            "triste" -> listOf("tranquilo", "reconfortante", "acogedor")
            "aventurero" -> listOf("aventura", "emocionante", "único")
            "relajado" -> listOf("calmado", "pacífico", "natural")
            "romántico", "romantico" -> listOf("romántico", "íntimo", "elegante")
            else -> emptyList()
        }

        val tagMatch = place.tags.count { it in moodTags }.toDouble() / moodTags.size.coerceAtLeast(1)
        score += tagMatch * 30

        val timeMatch = when (request.timeOfDay.lowercase()) {
            "mañana" -> if (place.category in listOf("cafe", "parque", "museo")) 1.0 else 0.5
            "tarde" -> if (place.category in listOf("restaurante", "parque")) 1.0 else 0.6
            "noche" -> if (place.category in listOf("bar", "restaurante", "teatro")) 1.0 else 0.4
            else -> 0.5
        }
        score += timeMatch * 25

        val categoryMatch = if (place.category in userContext.favoriteCategories) 1.0 else 0.6
        score += categoryMatch * 20

        val budgetScore = when (request.budget.lowercase()) {
            "economico" -> if (place.priceLevel <= 2) 1.0 else 0.3
            "moderado" -> if (place.priceLevel in 2..3) 1.0 else 0.6
            "alto" -> if (place.priceLevel >= 3) 1.0 else 0.7
            else -> 0.7
        }
        score += budgetScore * 15

        val distance = calculateDistance(
            request.userLocation.latitude, request.userLocation.longitude,
            place.latitude, place.longitude
        )
        val distanceScore = when {
            distance < 3.0 -> 1.0
            distance < 7.0 -> 0.8
            distance < 15.0 -> 0.5
            else -> 0.2
        }
        score += distanceScore * 10

        return score.coerceIn(0.0, 100.0)
    }

    // ========== FILTROS ==========

    private fun filterByContext(
        places: List<Place>,
        timeOfDay: String,
        weather: String?
    ): List<Place> {
        return places.filter { place ->
            val timeMatch = when (timeOfDay.lowercase()) {
                "mañana" -> place.category !in listOf("bar", "discoteca")
                "noche" -> true
                else -> true
            }

            val weatherMatch = if (weather != null) {
                when (weather.lowercase()) {
                    "lluvioso" -> place.category !in listOf("parque")
                    else -> true
                }
            } else true

            timeMatch && weatherMatch
        }
    }

    private fun filterByPersonalizedCriteria(
        places: List<Place>,
        mood: String,
        planType: String,
        budget: String,
        duration: String,
        timeOfDay: String,
        weather: String?
    ): List<Place> {
        return places.filter { place ->
            val budgetMatch = when (budget.lowercase()) {
                "economico" -> place.priceLevel <= 2
                "moderado" -> place.priceLevel in 2..3
                "alto" -> true
                else -> true
            }

            val planTypeMatch = when (planType.lowercase()) {
                "pareja" -> "romántico" in place.tags || place.category in listOf("restaurante", "cafe")
                "familia" -> "familiar" in place.tags || place.category in listOf("parque", "museo")
                "amigos" -> place.category in listOf("bar", "restaurante", "parque")
                else -> true
            }

            budgetMatch && planTypeMatch
        }
    }

    // ========== HELPERS ==========

    private suspend fun getUserContext(userId: String): UserContext {
        return try {
            val favorites = firestore.collection("favoritos")
                .document(userId)
                .collection("lugares")
                .limit(20)
                .get()
                .await()

            val categories = mutableListOf<String>()
            val tags = mutableListOf<String>()

            favorites.documents.forEach { doc ->
                doc.getString("category")?.let { categories.add(it) }
                (doc.get("tags") as? List<*>)?.mapNotNull { it as? String }?.let { tags.addAll(it) }
            }

            UserContext(
                favoriteCategories = categories.distinct(),
                frequentTags = tags.distinct(),
                averagePriceLevel = 2.0,
                lastVisitedPlaces = emptyList()
            )
        } catch (e: Exception) {
            UserContext()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return (results[0] / 1000.0)
    }

    private fun estimateDuration(distanceKm: Double): String {
        val minutes = (distanceKm * 15).toInt()
        return when {
            minutes < 60 -> "$minutes min"
            else -> "${minutes / 60}h ${minutes % 60}min"
        }
    }

    private fun generateQuickReason(place: Place, timeOfDay: String, weather: String?): String {
        val reasons = mutableListOf<String>()

        when (timeOfDay.lowercase()) {
            "mañana" -> reasons.add("perfecto para empezar el día")
            "tarde" -> reasons.add("ideal para esta hora")
            "noche" -> reasons.add("excelente para la noche")
        }

        if (weather != null) {
            when (weather.lowercase()) {
                "soleado" -> reasons.add("disfruta del buen clima")
                "lluvioso" -> reasons.add("refugio perfecto para la lluvia")
            }
        }

        if (place.rating >= 4.0) {
            reasons.add("muy bien valorado")
        }

        return reasons.joinToString(", ").replaceFirstChar { it.uppercase() }
    }

    private fun generatePersonalizedReason(
        place: Place,
        request: PersonalizedRecommendationRequest
    ): String {
        val reasons = mutableListOf<String>()

        when (request.mood.lowercase()) {
            "aventurero" -> reasons.add("una experiencia única te espera")
            "relajado" -> reasons.add("el lugar perfecto para desconectar")
            "romántico", "romantico" -> reasons.add("ambiente ideal para una cita especial")
            "feliz" -> reasons.add("para continuar con esa buena energía")
        }

        when (request.planType.lowercase()) {
            "pareja" -> reasons.add("romántico y acogedor")
            "familia" -> reasons.add("perfecto para toda la familia")
            "amigos" -> reasons.add("diversión asegurada con tu grupo")
        }

        if (place.rating >= 4.5) {
            reasons.add("excelentes reseñas")
        }

        return reasons.joinToString(", ").replaceFirstChar { it.uppercase() }
    }
}