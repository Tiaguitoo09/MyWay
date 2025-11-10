package com.example.myway.ai

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.myway.data.CacheManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random
import java.util.Calendar
import com.example.myway.screens.modulo4.PlaceCategory

class AIRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val cacheManager = CacheManager(context)
    private val recentRecommendations = mutableListOf<String>()
    private val maxHistorySize = 5

    // ========== RECOMENDACIÓN RÁPIDA ==========

    suspend fun getQuickRecommendation(
        request: QuickRecommendationRequest
    ): Result<AIRecommendation> {
        return try {
            val userContext = getUserContext(request.userId)
            val nearbyPlaces = searchNearbyPlaces(
                request.userLocation,
                radiusKm = 10.0
            )
            debugRecommendations(nearbyPlaces)

            if (nearbyPlaces.isEmpty()) {
                return Result.failure(Exception("No se encontraron lugares cercanos"))
            }

            Log.d("AIRepository", "📍 Encontrados ${nearbyPlaces.size} lugares")

            val filteredPlaces = filterByContext(
                places = nearbyPlaces,
                timeOfDay = request.timeOfDay,
                weather = request.currentWeather
            )

            Log.d("AIRepository", "✅ Filtrados: ${filteredPlaces.size} lugares")

            if (filteredPlaces.isEmpty()) {
                return Result.failure(Exception("No hay lugares disponibles para tus criterios"))
            }

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

            val selected = selectBestRecommendation(scoredPlaces)
                ?: return Result.failure(Exception("No se encontró una buena recomendación"))

            addToHistory(selected.place.id)
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

            debugRecommendations(nearbyPlaces)

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

            val selected = selectBestRecommendation(scoredPlaces)
                ?: return Result.failure(Exception("No se encontró ninguna recomendación"))

            addToHistory(selected.place.id)

            Result.success(selected)

        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== BÚSQUEDA DE LUGARES CON CACHÉ ==========

    private suspend fun searchNearbyPlaces(
        location: UserLocation,
        radiusKm: Double
    ): List<Place> {
        val allPlaces = mutableListOf<Place>()
        val googlePlaces = searchGooglePlacesWithCache(location, radiusKm)
        allPlaces.addAll(googlePlaces)
        Log.d("AIRepository", "🌍 Google Places: ${googlePlaces.size} lugares")

        return allPlaces
            .distinctBy { it.name.lowercase() }
            .filter { place ->
                val isValidCategory = place.category !in listOf("hotel", "hospedaje", "motel", "otro")

                val hasInvalidKeyword = listOf(
                    "hotel", "hostel", "motel", "inn", "hospedaje",
                    "hospital", "clínica", "farmacia",
                    "banco", "cajero",
                    "gasolinera", "estación de servicio"
                ).any { keyword ->
                    place.name.contains(keyword, ignoreCase = true)
                }

                val isValid = isValidCategory && !hasInvalidKeyword

                if (!isValid) {
                    Log.d("AIRepository", "❌ Filtrado: ${place.name} (${place.category})")
                }

                isValid
            }
    }


    private suspend fun searchGooglePlacesWithCache(
        location: UserLocation,
        radiusKm: Double
    ): List<Place> {
        val locationKey = cacheManager.generateLocationKey(
            location.latitude,
            location.longitude,
            radiusKm
        )

        cacheManager.getGooglePlacesCache(locationKey)?.let { cachedPlaces ->
            Log.d("AIRepository", "✅ Usando caché de Google Places")
            return cachedPlaces
        }

        Log.d("AIRepository", "⬇️ Buscando en Google Places...")
        return try {
            val places = GooglePlacesNearby.searchNearby(
                location = location,
                radiusMeters = (radiusKm * 1000).toInt(),
                types = listOf("restaurant", "cafe", "park", "museum", "bar", "shopping_mall", "night_club")
            )

            if (places.isNotEmpty()) {
                cacheManager.cacheGooglePlaces(locationKey, places)
            }

            places
        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error buscando en Google Places: ${e.message}")
            emptyList()
        }
    }

    // ========== SCORING MEJORADO ==========

    private fun calculateQuickScore(
        place: Place,
        userLocation: UserLocation,
        timeOfDay: String,
        weather: String?,
        userContext: UserContext
    ): Double {
        var score = 0.0

        // 1. DISTANCIA (35 puntos) - ¡AHORA ES LO MÁS IMPORTANTE!
        val distance = calculateDistance(
            userLocation.latitude, userLocation.longitude,
            place.latitude, place.longitude
        )
        val distanceScore = when {
            distance < 1.0 -> 1.0      // Menos de 1km = perfecto
            distance < 2.5 -> 0.9      // Menos de 2.5km = muy bueno
            distance < 5.0 -> 0.7      // Menos de 5km = bueno
            distance < 8.0 -> 0.5      // Menos de 8km = aceptable
            distance < 12.0 -> 0.3     // Menos de 12km = lejos
            else -> 0.1                // Más de 12km = muy lejos
        }
        score += distanceScore * 35
        Log.d("AIRepository", "${place.name} - Distance: $distanceScore (${distance.format(1)}km)")

        // 2. MATCHING POR HORA DEL DÍA (25 puntos)
        val timeScore = calculateTimeScore(place, timeOfDay)
        score += timeScore * 25
        Log.d("AIRepository", "${place.name} - Time: $timeScore")

        // 3. MATCHING POR CLIMA (15 puntos)
        val weatherScore = calculateWeatherScore(place, weather)
        score += weatherScore * 15
        Log.d("AIRepository", "${place.name} - Weather: $weatherScore")

        // 4. RATING AJUSTADO (10 puntos) - Menos peso para dar chance a lugares nuevos
        // Normalizar el rating para que lugares con 3.8-4.0 también tengan oportunidad
        val ratingScore = when {
            place.rating >= 4.5 -> 1.0
            place.rating >= 4.0 -> 0.95
            place.rating >= 3.8 -> 0.85
            place.rating >= 3.5 -> 0.75
            else -> 0.5
        }
        score += ratingScore * 10
        Log.d("AIRepository", "${place.name} - Rating: $ratingScore (${place.rating})")

        // 5. TAGS (8 puntos)
        val tagScore = calculateTagScore(place, timeOfDay, weather)
        score += tagScore * 8

        // 6. CATEGORÍAS FAVORITAS (5 puntos)
        val categoryBonus = if (place.category in userContext.favoriteCategories) 1.0 else 0.0
        score += categoryBonus * 5

        // 7. BONUS POR DESCUBRIMIENTO (2 puntos)
        // Dar un pequeño boost a lugares menos conocidos (rating entre 3.5-4.2)
        val discoveryBonus = when {
            place.rating in 3.5..4.2 && distance < 5.0 -> 1.0
            else -> 0.0
        }
        score += discoveryBonus * 2

        return score.coerceIn(0.0, 100.0)
    }

    private fun calculateTimeScore(place: Place, timeOfDay: String): Double {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        return when (timeOfDay.lowercase()) {
            "mañana" -> when (place.category) {
                "cafe" -> 1.0
                "parque" -> 0.95
                "museo" -> 0.9
                "restaurante" -> 0.7
                "centro_comercial" -> 0.8
                "bar" -> 0.0
                "discoteca" -> 0.0
                else -> 0.5
            }

            "tarde" -> when (place.category) {
                "restaurante" -> 1.0
                "parque" -> 0.9
                "centro_comercial" -> 0.95
                "museo" -> 0.85
                "cafe" -> 0.8
                "mirador" -> 0.9
                "bar" -> 0.3
                "discoteca" -> 0.0
                else -> 0.7
            }

            "noche" -> when (place.category) {
                "bar" -> if (isWeekend) 1.0 else 0.9
                "discoteca" -> if (isWeekend) 1.0 else 0.85
                "restaurante" -> 0.95
                "teatro" -> 0.9
                "centro_comercial" -> 0.7
                "cafe" -> 0.6
                "museo" -> 0.2
                "parque" -> 0.1
                else -> 0.4
            }

            else -> 0.5
        }
    }

    private fun calculateWeatherScore(place: Place, weather: String?): Double {
        if (weather == null) return 0.8

        if (place.weatherSuitable.isNotEmpty()) {
            return if (weather.lowercase() in place.weatherSuitable.map { it.lowercase() }) {
                1.0
            } else {
                0.4
            }
        }

        return when (weather.lowercase()) {
            "soleado" -> when (place.category) {
                "parque" -> 1.0
                "mirador" -> 1.0
                "mercado" -> 0.95
                "zona_comercial" -> 0.9
                "restaurante" -> 0.8
                "cafe" -> 0.85
                "museo" -> 0.7
                else -> 0.8
            }

            "lluvioso" -> when (place.category) {
                "museo" -> 1.0
                "cafe" -> 0.95
                "centro_comercial" -> 1.0
                "restaurante" -> 0.9
                "cine" -> 0.95
                "bar" -> 0.85
                "discoteca" -> 0.9
                "parque" -> 0.2
                "mirador" -> 0.1
                else -> 0.7
            }

            "nublado" -> when (place.category) {
                "museo" -> 0.95
                "restaurante" -> 0.9
                "cafe" -> 0.9
                "centro_comercial" -> 0.95
                "parque" -> 0.7
                else -> 0.85
            }

            else -> 0.8
        }
    }

    private fun calculateTagScore(place: Place, timeOfDay: String, weather: String?): Double {
        val relevantTags = mutableListOf<String>()

        when (timeOfDay.lowercase()) {
            "mañana" -> relevantTags.addAll(listOf("café especial", "tranquilo", "acogedor"))
            "tarde" -> relevantTags.addAll(listOf("familiar", "social", "popular"))
            "noche" -> relevantTags.addAll(listOf("nocturno", "vibrante", "romántico", "festivo"))
        }

        when (weather?.lowercase()) {
            "soleado" -> relevantTags.addAll(listOf("natural", "aire libre", "mirador"))
            "lluvioso" -> relevantTags.addAll(listOf("acogedor", "íntimo", "indoor"))
        }

        val matches = place.tags.count { tag ->
            relevantTags.any { it.contains(tag, ignoreCase = true) ||
                    tag.contains(it, ignoreCase = true) }
        }

        return if (relevantTags.isNotEmpty()) {
            (matches.toDouble() / relevantTags.size.coerceAtLeast(1)).coerceIn(0.0, 1.0)
        } else {
            0.5
        }
    }

    // ========== FILTROS MEJORADOS ==========

    private fun filterByContext(
        places: List<Place>,
        timeOfDay: String,
        weather: String?
    ): List<Place> {
        return places.filter { place ->

            // Validación de categoría base
            val validCategories = setOf(
                "restaurante", "cafe", "parque", "museo",
                "bar", "discoteca", "centro_comercial",
                "cine", "mirador", "zona_comercial",
                "mercado", "atraccion_turistica", "entretenimiento"
            )

            if (place.category !in validCategories) {
                Log.d("AIRepository", "❌ Categoría inválida: ${place.name} (${place.category})")
                return@filter false
            }

            // Filtro por hora del día - MÁS PERMISIVO
            val timeOk = when (timeOfDay.lowercase()) {
                "mañana" -> {
                    place.category !in listOf("discoteca", "bar")
                }
                "tarde" -> true // Todo vale en la tarde
                "noche" -> {
                    // Por la noche permitir más cosas
                    if (place.category == "parque") {
                        place.rating >= 3.8 // Menos restrictivo
                    } else {
                        true
                    }
                }
                else -> true
            }

            // Filtro por clima - MÁS PERMISIVO
            val weatherOk = when (weather?.lowercase()) {
                "lluvioso" -> {
                    // Con lluvia evitar lugares al aire libre sin cobertura
                    if (place.category == "parque") {
                        "lluvioso" in place.weatherSuitable.map { it.lowercase() }
                    } else {
                        true
                    }
                }
                else -> true
            }

            // Rating mínimo REDUCIDO para dar más oportunidades
            val ratingOk = place.rating >= 3.3 // Antes era 3.5

            val passes = timeOk && weatherOk && ratingOk

            if (!passes) {
                Log.d("AIRepository", "❌ Filtrado contexto: ${place.name} - time:$timeOk weather:$weatherOk rating:$ratingOk")
            }

            passes
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
            val budgetOk = when (budget.lowercase()) {
                "economico" -> place.priceLevel <= 2
                "moderado" -> place.priceLevel in 1..3
                "alto" -> true
                else -> true
            }

            val planTypeOk = when (planType.lowercase()) {
                "pareja" -> {
                    "romántico" in place.tags ||
                            "íntimo" in place.tags ||
                            place.category in listOf("restaurante", "cafe", "bar", "teatro", "parque")
                }
                "familia" -> {
                    "familiar" in place.tags ||
                            place.category in listOf("parque", "museo", "centro_comercial", "atraccion_turistica")
                }
                "amigos" -> {
                    place.category in listOf("bar", "restaurante", "parque", "discoteca", "zona_comercial", "cafe")
                }
                "solo" -> true // Casi todo vale cuando vas solo
                else -> true
            }

            // Rating mínimo más bajo
            val ratingOk = place.rating >= 3.3

            budgetOk && planTypeOk && ratingOk
        }
    }

    private fun checkMoodMatch(place: Place, mood: String): Boolean {
        val moodTags = when (mood.lowercase()) {
            "feliz" -> listOf("vibrante", "social", "festivo", "energético")
            "triste" -> listOf("tranquilo", "acogedor", "reconfortante")
            "aventurero" -> listOf("único", "aventura", "emocionante", "mirador")
            "relajado" -> listOf("tranquilo", "natural", "pacífico", "café especial")
            "romántico", "romantico" -> listOf("romántico", "íntimo", "elegante")
            else -> return true
        }

        return place.tags.any { tag ->
            moodTags.any { it.contains(tag, ignoreCase = true) ||
                    tag.contains(it, ignoreCase = true) }
        }
    }

    // ========== SCORING PERSONALIZADO ==========

    private fun calculatePersonalizedScore(
        place: Place,
        request: PersonalizedRecommendationRequest,
        userContext: UserContext
    ): Double {
        var score = 0.0

        // 1. DISTANCIA (30 puntos) - Prioridad en personalizado también
        val distance = calculateDistance(
            request.userLocation.latitude, request.userLocation.longitude,
            place.latitude, place.longitude
        )
        val distanceScore = when {
            distance < 2.0 -> 1.0
            distance < 4.0 -> 0.9
            distance < 7.0 -> 0.75
            distance < 10.0 -> 0.6
            distance < 15.0 -> 0.4
            else -> 0.2
        }
        score += distanceScore * 30

        // 2. MOOD (20 puntos)
        val moodScore = calculateMoodScore(place, request.mood)
        score += moodScore * 20

        // 3. MATCHING POR HORA DEL DÍA (15 puntos)
        val timeScore = calculateTimeScore(place, request.timeOfDay)
        score += timeScore * 15

        // 4. TIPO DE PLAN (12 puntos)
        val planScore = calculatePlanTypeScore(place, request.planType)
        score += planScore * 12

        // 5. CLIMA (10 puntos)
        val weatherScore = calculateWeatherScore(place, request.currentWeather)
        score += weatherScore * 10

        // 6. RATING AJUSTADO (8 puntos)
        val ratingScore = when {
            place.rating >= 4.5 -> 1.0
            place.rating >= 4.0 -> 0.9
            place.rating >= 3.8 -> 0.8
            place.rating >= 3.5 -> 0.7
            else -> 0.5
        }
        score += ratingScore * 8

        // 7. PRESUPUESTO (3 puntos)
        val budgetScore = when (request.budget.lowercase()) {
            "economico" -> if (place.priceLevel <= 2) 1.0 else 0.5
            "moderado" -> if (place.priceLevel in 2..3) 1.0 else 0.7
            "alto" -> if (place.priceLevel >= 3) 1.0 else 0.8
            else -> 0.8
        }
        score += budgetScore * 3

        // 8. BONUS DESCUBRIMIENTO (2 puntos)
        val discoveryBonus = when {
            place.rating in 3.5..4.2 && distance < 7.0 -> 1.0
            else -> 0.0
        }
        score += discoveryBonus * 2

        return score.coerceIn(0.0, 100.0)
    }

    private fun calculateMoodScore(place: Place, mood: String): Double {
        val moodTags = when (mood.lowercase()) {
            "feliz" -> listOf("vibrante", "social", "festivo", "energético", "popular")
            "triste" -> listOf("tranquilo", "acogedor", "reconfortante", "café", "natural")
            "aventurero" -> listOf("único", "aventura", "emocionante", "mirador", "cultural")
            "relajado" -> listOf("tranquilo", "natural", "pacífico", "café especial", "acogedor")
            "romántico", "romantico" -> listOf("romántico", "íntimo", "elegante", "gourmet", "premium")
            else -> return 0.7
        }

        val exactMatches = place.tags.count { tag ->
            moodTags.any { it.equals(tag, ignoreCase = true) }
        }

        val partialMatches = place.tags.count { tag ->
            moodTags.any {
                !it.equals(tag, ignoreCase = true) &&
                        (it.contains(tag, ignoreCase = true) || tag.contains(it, ignoreCase = true))
            }
        }

        val totalScore = (exactMatches * 1.0 + partialMatches * 0.5) / moodTags.size.coerceAtLeast(1)
        return totalScore.coerceIn(0.0, 1.0)
    }

    private fun calculatePlanTypeScore(place: Place, planType: String): Double {
        return when (planType.lowercase()) {
            "pareja" -> when {
                "romántico" in place.tags || "íntimo" in place.tags -> 1.0
                place.category in listOf("restaurante", "cafe", "bar", "teatro") -> 0.8
                else -> 0.5
            }
            "familia" -> when {
                "familiar" in place.tags -> 1.0
                place.category in listOf("parque", "museo", "centro_comercial") -> 0.9
                else -> 0.5
            }
            "amigos" -> when {
                place.category in listOf("bar", "discoteca", "restaurante", "zona_comercial") -> 0.95
                "social" in place.tags || "vibrante" in place.tags -> 0.9
                else -> 0.6
            }
            "solo" -> when {
                place.category in listOf("cafe", "museo", "parque") -> 0.9
                "tranquilo" in place.tags -> 0.85
                else -> 0.7
            }
            else -> 0.7
        }
    }

    // ========== SELECCIÓN Y HELPERS ==========

    private fun selectBestRecommendation(recommendations: List<AIRecommendation>): AIRecommendation? {
        if (recommendations.isEmpty()) return null

        // Filtrar lugares recientemente recomendados
        val notRecent = recommendations.filter { it.place.id !in recentRecommendations }
        val available = notRecent.ifEmpty { recommendations }

        // Ordenar por score
        val sorted = available.sortedByDescending { it.score }

        // Tomar los mejores candidatos
        val topCandidates = sorted.take(8) // Aumentado de 5 a 8 para más variedad

        return when {
            topCandidates.size == 1 -> topCandidates.first()

            topCandidates.size > 1 -> {
                val random = Random.nextDouble()

                when {
                    // 50% de probabilidad: el mejor
                    random < 0.50 -> topCandidates.first()

                    // 30% de probabilidad: entre los lugares 2-4 (cercanos pero menos conocidos)
                    random < 0.80 -> {
                        val midRange = topCandidates.subList(1, minOf(4, topCandidates.size))
                        midRange.randomOrNull() ?: topCandidates.first()
                    }

                    // 20% de probabilidad: entre los lugares 4-8 (descubrimiento)
                    else -> {
                        val discovery = topCandidates.subList(
                            minOf(3, topCandidates.size),
                            topCandidates.size
                        )
                        discovery.randomOrNull() ?: topCandidates.first()
                    }
                }
            }

            else -> null
        }
    }

    private fun addToHistory(placeId: String) {
        recentRecommendations.add(placeId)
        if (recentRecommendations.size > maxHistorySize) {
            recentRecommendations.removeAt(0)
        }
    }

    fun clearRecommendationHistory() {
        recentRecommendations.clear()
        Log.d("AIRepository", "🧹 Historial limpiado")
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

    private suspend fun getUserContext(userId: String): UserContext {
        // Sin Firebase - contexto vacío
        return UserContext(
            favoriteCategories = emptyList(),
            frequentTags = emptyList(),
            averagePriceLevel = 2.0,
            lastVisitedPlaces = emptyList()
        )
    }

    // ========== MÉTODOS DE CACHÉ ==========

    fun cleanExpiredCache() {
        cacheManager.cleanExpiredCache()
    }

    suspend fun forceRefresh(location: UserLocation, radiusKm: Double) {
        cacheManager.clearMemoryCache()
        clearRecommendationHistory()
        searchNearbyPlaces(location, radiusKm)
    }

    fun clearAllCache() {
        cacheManager.clearAllCache()
        clearRecommendationHistory()
    }

    fun getCacheStats(): CacheManager.CacheStats {
        return cacheManager.getCacheStats()
    }

    // ========== RANKING TOP LUGARES ==========

    suspend fun getTopPlaces(
        location: UserLocation,
        radiusKm: Double = 10.0,
        limit: Int = 10
    ): List<Place> {
        return try {
            val allPlaces = searchNearbyPlaces(location, radiusKm)

            if (allPlaces.isEmpty()) {
                Log.w("AIRepository", "⚠️ No se encontraron lugares para ranking")
                return emptyList()
            }

            // 🔹 Tomamos los mejores por calificación
            val topBasicPlaces = allPlaces
                .filter { it.rating > 0 }
                .sortedByDescending { it.rating }
                .take(limit)

            // 🔹 Por cada lugar, pedimos sus detalles completos
            topBasicPlaces.mapNotNull { place ->
                val details = getPlaceDetails(place.id)
                details ?: place  // si no hay detalles, usa los básicos
            }

        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error obteniendo ranking: ${e.message}")
            emptyList()
        }
    }




        suspend fun getTopPlaceRecommendations(
        location: UserLocation,
        radiusKm: Double = 10.0,
        limit: Int = 10
    ): List<PlaceRecommendation> {
        val topPlaces = getTopPlaces(location, radiusKm, limit)

        return topPlaces.map { place ->
            PlaceRecommendation(
                placeId = place.id,  
                nombre = place.name,
                ciudad = place.address.split(",").lastOrNull()?.trim() ?: "Desconocida",
                descripcion = "Muy recomendado por los visitantes.",
                calificacion = place.rating,
                categoria = place.category,
                razon = "Alta valoración y popularidad en la zona."
            )
        }
    }

        suspend fun getTopPlacesAI(limit: Int = 10): List<PlaceRecommendation> {

        val fakeLocation = UserLocation(latitude = 4.7110, longitude = -74.0721)
        return getTopPlaceRecommendations(fakeLocation, radiusKm = 10.0, limit = limit)
    }

    // ========== DETALLES DE UN LUGAR ESPECÍFICO ==========
    suspend fun getPlaceDetails(placeId: String): Place? {
        return try {

            val apiKey = "MAPS_API_KEY"
            val url =
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=name,formatted_address,rating,geometry,photos,types&key=$apiKey"

            // Conexión HTTP manual (sin Retrofit)
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"

            val response = connection.inputStream.bufferedReader().use { it.readText() }


            val json = org.json.JSONObject(response)
            val result = json.optJSONObject("result") ?: return null


            val name = result.optString("name", "Sin nombre")
            val address = result.optString("formatted_address", "Sin dirección")
            val rating = result.optDouble("rating", 0.0)


            val geometry = result.optJSONObject("geometry")
            val location = geometry?.optJSONObject("location")
            val lat = location?.optDouble("lat", 0.0) ?: 0.0
            val lng = location?.optDouble("lng", 0.0) ?: 0.0


            val typesArray = result.optJSONArray("types")
            val category = if (typesArray != null && typesArray.length() > 0)
                typesArray.getString(0)
            else "Sin categoría"


            val photosArray = result.optJSONArray("photos")
            val photoUrl = if (photosArray != null && photosArray.length() > 0) {
                val photoReference =
                    photosArray.getJSONObject(0).optString("photo_reference")
                buildPhotoUrl(photoReference)
            } else null


            Place(
                id = placeId,
                name = name,
                address = address,
                latitude = lat,
                longitude = lng,
                photoUrl = photoUrl,
                category = category,
                priceLevel = 0,
                rating = rating,
                tags = emptyList(),
                weatherSuitable = emptyList()
            )

        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "❌ Error al obtener detalles del lugar: ${e.message}")
            null
        }
    }


    private fun buildPhotoUrl(photoReference: String): String {
        val apiKey = "MAPS_API_KEY"
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=$photoReference&key=$apiKey"
    }

    // ========== AGREGAR ESTAS FUNCIONES AL AIRepository.kt ==========

    suspend fun getTopPlacesByCategory(
        location: UserLocation,
        category: PlaceCategory,
        radiusKm: Double = 15.0,
        limit: Int = 15
    ): List<Place> {
        return try {
            Log.d("AIRepository", "🔍 Buscando ${category.displayName} en radio de ${radiusKm}km")

            // Obtener todos los lugares cercanos (incluye hoteles solo para ranking)
            val allPlaces = searchNearbyPlacesForRanking(location, radiusKm)

            // Filtrar por categoría con match más flexible
            val filteredPlaces = allPlaces.filter { place ->
                category.categories.any { cat ->
                    place.category.equals(cat, ignoreCase = true) ||
                            place.category.contains(cat, ignoreCase = true)
                }
            }

            Log.d("AIRepository", "📊 Encontrados ${filteredPlaces.size} lugares de ${category.displayName}")

            if (filteredPlaces.isEmpty()) {
                return emptyList()
            }

            // Calcular score y ordenar
            filteredPlaces
                .map { place ->
                    val distance = calculateDistance(
                        location.latitude, location.longitude,
                        place.latitude, place.longitude
                    )

                    ScoredPlace(
                        place = place,
                        score = calculateRankingScore(place, distance),
                        distance = distance
                    )
                }
                .sortedByDescending { it.score }
                .take(limit)
                .also { topPlaces ->
                    // Log de resultados
                    topPlaces.forEachIndexed { index, scored ->
                        Log.d(
                            "AIRepository",
                            "#${index + 1} ${scored.place.name} - Score: ${scored.score.format(1)} " +
                                    "(${scored.distance.format(1)}km, ⭐${scored.place.rating})"
                        )
                    }
                }
                .map { it.place }

        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error obteniendo ranking: ${e.message}", e)
            emptyList()
        }
    }


    private suspend fun searchNearbyPlacesForRanking(
        location: UserLocation,
        radiusKm: Double
    ): List<Place> {
        val allPlaces = mutableListOf<Place>()
        val googlePlaces = searchGooglePlacesForRanking(location, radiusKm)


        allPlaces.addAll(googlePlaces)



        // Deduplicar y filtrar en un solo paso
        return allPlaces
            .distinctBy { it.name.lowercase().trim() }
            .filter { it.rating >= 3.0 }
    }

    /**
     * Buscar en Google Places SIN filtrar hoteles
     */
    private suspend fun searchGooglePlacesForRanking(
        location: UserLocation,
        radiusKm: Double
    ): List<Place> {
        Log.d("AIRepository", "⬇️ Google Places (ranking)...")
        return try {
            GooglePlacesNearby.searchNearbyForRanking(
                location = location,
                radiusMeters = (radiusKm * 1000).toInt()
            )
        } catch (e: Exception) {
            Log.e("AIRepository", "❌ Error Google Places: ${e.message}")
            emptyList()
        }
    }

    /**
     * Calcula el score para ranking
     * 50% Distancia + 30% Rating + 20% Popularidad
     */
    private fun calculateRankingScore(
        place: Place,
        distance: Double
    ): Double {
        // 1. DISTANCIA (55 puntos) - AUMENTADO para priorizar cercanía
        val distanceScore = when {
            distance < 0.5 -> 1.0      // Muy cerca
            distance < 1.0 -> 0.95     // Caminable
            distance < 2.0 -> 0.90     // Cerca
            distance < 3.0 -> 0.80     // Accesible
            distance < 5.0 -> 0.65     // Aceptable
            distance < 7.0 -> 0.50     // Algo lejos
            distance < 10.0 -> 0.35    // Lejos
            distance < 15.0 -> 0.20    // Muy lejos
            else -> 0.05               // Demasiado lejos
        }

        // 2. RATING (30 puntos)
        val ratingScore = when {
            place.rating >= 4.7 -> 1.0
            place.rating >= 4.5 -> 0.95
            place.rating >= 4.3 -> 0.90
            place.rating >= 4.0 -> 0.85
            place.rating >= 3.8 -> 0.75
            place.rating >= 3.5 -> 0.65
            place.rating >= 3.0 -> 0.50
            else -> 0.30
        }

        // 3. POPULARIDAD (15 puntos)
        val popularityScore = calculatePopularityScore(place)

        return (distanceScore * 55 + ratingScore * 30 + popularityScore * 15).coerceIn(0.0, 100.0)
    }

    /**
     * Calcula score de popularidad basado en tags y otros factores
     */
    private fun calculatePopularityScore(place: Place): Double {
        var score = 0.0

        // Tags importantes (40%)
        val importantTags = setOf(
            "popular", "recomendado", "excelente", "premium",
            "único", "imperdible", "turístico", "famoso"
        )

        val tagMatches = place.tags.count { tag ->
            importantTags.any { important ->
                tag.contains(important, ignoreCase = true) || important.contains(tag, ignoreCase = true)
            }
        }
        score += (tagMatches.toDouble() / 4.0).coerceAtMost(1.0) * 0.4

        // Cantidad de información (30%)
        val infoScore = minOf(place.tags.size / 10.0, 1.0) * 0.3
        score += infoScore

        // Tiene foto (30%)
        if (place.photoUrl != null) score += 0.3

        return score.coerceIn(0.0, 1.0)
    }

    // Data class auxiliar para scoring
    private data class ScoredPlace(
        val place: Place,
        val score: Double,
        val distance: Double
    )

    // Helper para formatear números
    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)



}

fun debugRecommendations(places: List<Place>) {
    Log.d("AIRepository", "=== DEBUG LUGARES ===")
    val byCategory = places.groupBy { it.category }
    byCategory.forEach { (category, list) ->
        Log.d("AIRepository", "$category: ${list.size} lugares")
        list.take(3).forEach { place ->
            Log.d("AIRepository", "  - ${place.name} (${place.rating}⭐)")
        }
    }
}


