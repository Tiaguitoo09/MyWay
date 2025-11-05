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

// dentro del mismo archivo donde defines data class Place(...)

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?,
    val category: String,
    val priceLevel: Int,
    val rating: Double,
    val tags: List<String>,
    val weatherSuitable: List<String>,
    val phoneNumber: String? = null     // <-- NUEVO campo (nullable)
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): Place {
            return Place(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                address = map["address"] as? String ?: "",
                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
                photoUrl = map["photoUrl"] as? String,
                category = map["category"] as? String ?: "",
                priceLevel = (map["priceLevel"] as? Number)?.toInt() ?: 0,
                rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                weatherSuitable = (map["weatherSuitable"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                phoneNumber = map["phoneNumber"] as? String ?: map["phone"] as? String // aceptar ambos nombres
            )
        }
    }
}

// fuera de la clase
fun Place.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "address" to address,
        "latitude" to latitude,
        "longitude" to longitude,
        "photoUrl" to photoUrl,
        "category" to category,
        "priceLevel" to priceLevel,
        "rating" to rating,
        "tags" to tags,
        "weatherSuitable" to weatherSuitable,
        "phoneNumber" to phoneNumber
    )
}




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


data class PlaceRecommendation(
    val placeId: String,
    val nombre: String,
    val ciudad: String,
    val descripcion: String,
    val calificacion: Double,
    val categoria: String,
    val razon: String
)

/**
 * Modelo principal de un plan de viaje
 */
data class TravelPlan(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val destino: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val duracion: Int = 1, // días
    val itinerario: String = "", // Texto completo generado por IA
    val actividades: List<DayActivity> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Convertir a Map para guardar en Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "titulo" to titulo,
            "destino" to destino,
            "fechaInicio" to fechaInicio,
            "fechaFin" to fechaFin,
            "duracion" to duracion,
            "itinerario" to itinerario,
            "actividades" to actividades.map { it.toMap() },
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        // Convertir desde Firestore
        fun fromMap(map: Map<String, Any?>): TravelPlan {
            return TravelPlan(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                titulo = map["titulo"] as? String ?: "",
                destino = map["destino"] as? String ?: "",
                fechaInicio = map["fechaInicio"] as? String ?: "",
                fechaFin = map["fechaFin"] as? String ?: "",
                duracion = (map["duracion"] as? Long)?.toInt() ?: 1,
                itinerario = map["itinerario"] as? String ?: "",
                actividades = (map["actividades"] as? List<*>)?.mapNotNull { item ->
                    (item as? Map<*, *>)?.let { activityMap ->
                        DayActivity.fromMap(activityMap as Map<String, Any?>)
                    }
                } ?: emptyList(),
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Actividad de un día específico
 */
data class DayActivity(
    val dia: Int = 1,
    val fecha: String = "",
    val actividades: List<ItineraryActivity> = emptyList()
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "dia" to dia,
            "fecha" to fecha,
            "actividades" to actividades.map { it.toMap() }
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): DayActivity {
            return DayActivity(
                dia = (map["dia"] as? Long)?.toInt() ?: 1,
                fecha = map["fecha"] as? String ?: "",
                actividades = (map["actividades"] as? List<*>)?.mapNotNull { item ->
                    (item as? Map<*, *>)?.let { actMap ->
                        ItineraryActivity.fromMap(actMap as Map<String, Any?>)
                    }
                } ?: emptyList()
            )
        }
    }
}

/**
 * Actividad individual dentro de un día
 */
data class ItineraryActivity(
    val hora: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lugar: String = "",
    val tipo: String = "" // desayuno, almuerzo, cena, visita, transporte, etc.
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "hora" to hora,
            "titulo" to titulo,
            "descripcion" to descripcion,
            "lugar" to lugar,
            "tipo" to tipo
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): ItineraryActivity {
            return ItineraryActivity(
                hora = map["hora"] as? String ?: "",
                titulo = map["titulo"] as? String ?: "",
                descripcion = map["descripcion"] as? String ?: "",
                lugar = map["lugar"] as? String ?: "",
                tipo = map["tipo"] as? String ?: ""
            )
        }
    }
}