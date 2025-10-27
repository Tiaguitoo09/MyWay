package com.example.myway.ai

/**
 * Modelos de datos para el sistema de IA
 */

// ========== UBICACIÓN ==========

data class UserLocation(
    val latitude: Double,
    val longitude: Double
)

// ========== LUGAR ==========

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?,
    val category: String, // restaurante, cafe, parque, museo, bar, etc.
    val priceLevel: Int, // 0-4 (0=gratis, 4=muy caro)
    val rating: Double, // 0.0-5.0
    val tags: List<String>, // vibrante, tranquilo, romántico, familiar, etc.
    val weatherSuitable: List<String> // soleado, nublado, lluvioso
)

// ========== REQUESTS DE RECOMENDACIÓN ==========

/**
 * Request para recomendación rápida (sin configuración)
 */
data class QuickRecommendationRequest(
    val userLocation: UserLocation,
    val currentWeather: String?, // soleado, nublado, lluvioso
    val timeOfDay: String, // mañana, tarde, noche
    val userId: String
)

/**
 * Request para recomendación personalizada (Tu Mood)
 */
data class PersonalizedRecommendationRequest(
    val userLocation: UserLocation,
    val mood: String, // feliz, triste, aventurero, relajado, romántico
    val planType: String, // solo, pareja, amigos, familia
    val budget: String, // economico, moderado, alto
    val duration: String, // corto (<2h), medio (2-4h), largo (>4h)
    val currentWeather: String?,
    val timeOfDay: String,
    val userId: String
)

// ========== RESPONSES DE RECOMENDACIÓN ==========

/**
 * Recomendación generada por la IA
 */
data class AIRecommendation(
    val place: Place,
    val score: Double, // 0-100
    val reason: String, // Explicación de por qué se recomienda
    val distance: Double, // Distancia en km
    val estimatedDuration: String // Tiempo estimado para llegar
)

/**
 * Detalles adicionales del lugar recomendado
 * (obtenidos de Google Places API)
 */
data class RecommendedPlaceDetails(
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double,
    val totalRatings: Int,
    val isOpen: Boolean?,
    val openingHours: List<String>
)

// ========== CONTEXTO DEL USUARIO ==========

/**
 * Información del historial y preferencias del usuario
 */
data class UserContext(
    val favoriteCategories: List<String> = emptyList(),
    val frequentTags: List<String> = emptyList(),
    val averagePriceLevel: Double = 2.0,
    val lastVisitedPlaces: List<String> = emptyList()
)

// ========== SCORING ==========

/**
 * Pesos para el cálculo de scores
 */
data class ScoringWeights(
    val categoryMatch: Double = 0.25,
    val tagMatch: Double = 0.20,
    val rating: Double = 0.15,
    val distance: Double = 0.15,
    val priceMatch: Double = 0.10,
    val timeContext: Double = 0.10,
    val weatherMatch: Double = 0.05
)

// ========== MODELOS PARA ITINERARIOS ==========

/**
 * Request para generar itinerario de viaje
 */
data class ItineraryRequest(
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budget: String, // "economico", "moderado", "alto"
    val interests: List<String>, // ["cultura", "aventura", "gastronomía", "naturaleza", etc.]
    val travelStyle: String, // "relajado", "intenso", "balanceado"
    val companions: String, // "solo", "pareja", "familia", "amigos"
    val userId: String
)

/**
 * Response con itinerario generado
 */
data class ItineraryResponse(
    val itinerary: String, // Texto completo del itinerario
    val dayByDay: List<DayPlan>,
    val estimatedCost: String,
    val recommendations: List<String>,
    val timestamp: Long
)

/**
 * Plan para un día específico
 */
data class DayPlan(
    val day: Int,
    val title: String,
    val activities: List<Activity>,
    val meals: List<Meal>
)

/**
 * Actividad dentro de un día
 */
data class Activity(
    val time: String, // "Mañana", "Tarde", "Noche"
    val name: String,
    val description: String,
    val location: String?,
    val estimatedCost: String?,
    val duration: String
)

/**
 * Comida recomendada
 */
data class Meal(
    val type: String, // "desayuno", "almuerzo", "cena"
    val recommendation: String,
    val estimatedCost: String
)