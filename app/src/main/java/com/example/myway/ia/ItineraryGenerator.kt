package com.example.myway.ai

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generador de itinerarios GRATUITO
 * No requiere API de OpenAI, usa lógica propia
 */
class ItineraryGenerator(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Genera un itinerario completo para un viaje
     */
    suspend fun generateItinerary(
        destination: String,
        startDate: String,
        endDate: String,
        budget: String = "moderado",
        interests: List<String> = emptyList()
    ): ItineraryResponse {
        Log.d("ItineraryGenerator", "🎯 Generando itinerario para $destination")

        // 1. Calcular días de viaje
        val numDays = calculateDays(startDate, endDate)
        Log.d("ItineraryGenerator", "📅 Duración: $numDays días")

        // 2. Obtener lugares del destino
        val places = getPlacesForDestination(destination, interests)
        Log.d("ItineraryGenerator", "📍 Encontrados ${places.size} lugares")

        // 3. Organizar lugares por tipo
        val categorizedPlaces = categorizePlaces(places)

        // 4. Generar plan día por día
        val dayPlans = mutableListOf<DayPlan>()
        val currentDate = parseDate(startDate)

        for (day in 1..numDays) {
            val dayPlan = generateDayPlan(
                day = day,
                date = formatDate(currentDate),
                categorizedPlaces = categorizedPlaces,
                isFirstDay = day == 1,
                isLastDay = day == numDays
            )
            dayPlans.add(dayPlan)

            // Avanzar al siguiente día
            currentDate.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 5. Generar texto del itinerario
        val itineraryText = buildItineraryText(
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            dayPlans = dayPlans
        )

        // 6. Calcular costo estimado
        val estimatedCost = calculateEstimatedCost(numDays, budget)

        // 7. Generar recomendaciones
        val recommendations = generateRecommendations(destination, numDays)

        return ItineraryResponse(
            itinerary = itineraryText,
            dayByDay = dayPlans,
            estimatedCost = estimatedCost,
            recommendations = recommendations,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Obtener lugares relevantes para el destino
     */
    private suspend fun getPlacesForDestination(
        destination: String,
        interests: List<String>
    ): List<Place> {
        return try {
            // Buscar en Firebase
            val snapshot = firestore.collection("lugares")
                .whereGreaterThanOrEqualTo("rating", 3.5)
                .limit(30)
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
                        weatherSuitable = emptyList()
                    )
                } catch (e: Exception) {
                    null
                }
            }.filter { place ->
                // Filtrar por categorías válidas
                place.category in listOf("restaurante", "cafe", "museo", "parque", "mirador", "centro_comercial")
            }
        } catch (e: Exception) {
            Log.e("ItineraryGenerator", "Error obteniendo lugares: ${e.message}")
            emptyList()
        }
    }

    /**
     * Categorizar lugares por tipo
     */
    private fun categorizePlaces(places: List<Place>): Map<String, List<Place>> {
        return places.groupBy { it.category }
    }

    /**
     * Generar plan para un día específico
     */
    private fun generateDayPlan(
        day: Int,
        date: String,
        categorizedPlaces: Map<String, List<Place>>,
        isFirstDay: Boolean,
        isLastDay: Boolean
    ): DayPlan {
        val activities = mutableListOf<Activity>()
        val meals = mutableListOf<Meal>()

        // Título del día
        val title = when {
            isFirstDay -> "Llegada y exploración"
            isLastDay -> "Último día y regreso"
            else -> "Día $day de exploración"
        }

        // MAÑANA (9:00 - 12:00)
        if (isFirstDay) {
            activities.add(
                Activity(
                    time = "09:00",
                    name = "Llegada y check-in",
                    description = "Instalación en el alojamiento y preparación para el día",
                    location = null,
                    estimatedCost = null,
                    duration = "2 horas"
                )
            )
        } else {
            // Actividad cultural o natural
            val morningPlace = selectPlace(categorizedPlaces, listOf("museo", "parque", "mirador"))
            if (morningPlace != null) {
                activities.add(
                    Activity(
                        time = "09:00",
                        name = morningPlace.name,
                        description = "Visita y exploración de ${morningPlace.name}",
                        location = morningPlace.address,
                        estimatedCost = when (morningPlace.priceLevel) {
                            1 -> "$15.000 - $25.000"
                            2 -> "$25.000 - $50.000"
                            3 -> "$50.000 - $100.000"
                            else -> "$30.000"
                        },
                        duration = "3 horas"
                    )
                )
            }
        }

        // ALMUERZO (12:30)
        val lunchPlace = selectPlace(categorizedPlaces, listOf("restaurante", "cafe"))
        meals.add(
            Meal(
                type = "almuerzo",
                recommendation = lunchPlace?.name ?: "Restaurante local recomendado",
                estimatedCost = "$35.000 - $60.000"
            )
        )

        // TARDE (14:00 - 18:00)
        val afternoonPlace = selectPlace(categorizedPlaces, listOf("centro_comercial", "museo", "parque"))
        if (afternoonPlace != null) {
            activities.add(
                Activity(
                    time = "14:30",
                    name = afternoonPlace.name,
                    description = "Tiempo libre para disfrutar de ${afternoonPlace.name}",
                    location = afternoonPlace.address,
                    estimatedCost = "$20.000 - $40.000",
                    duration = "3 horas"
                )
            )
        }

        // CENA (19:00)
        val dinnerPlace = selectPlace(categorizedPlaces, listOf("restaurante"))
        meals.add(
            Meal(
                type = "cena",
                recommendation = dinnerPlace?.name ?: "Restaurante local con comida típica",
                estimatedCost = "$40.000 - $80.000"
            )
        )

        // NOCHE (21:00)
        if (!isLastDay) {
            activities.add(
                Activity(
                    time = "21:00",
                    name = "Tiempo libre",
                    description = "Paseo nocturno por la zona o descanso en el alojamiento",
                    location = null,
                    estimatedCost = null,
                    duration = "2 horas"
                )
            )
        }

        return DayPlan(
            day = day,
            title = title,
            activities = activities,
            meals = meals
        )
    }

    /**
     * Seleccionar un lugar de categorías específicas
     */
    private fun selectPlace(
        categorizedPlaces: Map<String, List<Place>>,
        preferredCategories: List<String>
    ): Place? {
        for (category in preferredCategories) {
            val places = categorizedPlaces[category]
            if (!places.isNullOrEmpty()) {
                return places.random()
            }
        }
        return null
    }

    /**
     * Construir texto completo del itinerario
     */
    private fun buildItineraryText(
        destination: String,
        startDate: String,
        endDate: String,
        dayPlans: List<DayPlan>
    ): String {
        val sb = StringBuilder()

        sb.appendLine("# Itinerario: $destination")
        sb.appendLine()
        sb.appendLine("**Fechas:** $startDate al $endDate")
        sb.appendLine("**Duración:** ${dayPlans.size} días")
        sb.appendLine()
        sb.appendLine("## Resumen")
        sb.appendLine("Este itinerario te llevará a conocer lo mejor de $destination, combinando cultura, gastronomía y entretenimiento.")
        sb.appendLine()

        dayPlans.forEach { dayPlan ->
            sb.appendLine("## Día ${dayPlan.day}: ${dayPlan.title}")
            sb.appendLine()

            dayPlan.activities.forEach { activity ->
                sb.appendLine("### ${activity.time} - ${activity.name}")
                sb.appendLine("**Descripción:** ${activity.description}")
                if (activity.location != null) {
                    sb.appendLine("**Ubicación:** ${activity.location}")
                }
                if (activity.estimatedCost != null) {
                    sb.appendLine("**Costo estimado:** ${activity.estimatedCost}")
                }
                sb.appendLine("**Duración:** ${activity.duration}")
                sb.appendLine()
            }

            sb.appendLine("### Comidas recomendadas:")
            dayPlan.meals.forEach { meal ->
                sb.appendLine("- **${meal.type.replaceFirstChar { it.uppercase() }}:** ${meal.recommendation} - ${meal.estimatedCost}")
            }
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Calcular costo estimado del viaje
     */
    private fun calculateEstimatedCost(numDays: Int, budget: String): String {
        val dailyCost = when (budget.lowercase()) {
            "economico" -> 100000
            "moderado" -> 200000
            "alto" -> 400000
            else -> 200000
        }

        val totalCost = dailyCost * numDays

        return """
        **Desglose de costos estimados:**
        
        - Alojamiento: ${formatCurrency(totalCost * 0.35)}
        - Comidas: ${formatCurrency(totalCost * 0.30)}
        - Actividades: ${formatCurrency(totalCost * 0.25)}
        - Transporte: ${formatCurrency(totalCost * 0.10)}
        
        **Total estimado:** ${formatCurrency(totalCost.toDouble())}
        
        *Nota: Estos son costos aproximados y pueden variar según la temporada y tus preferencias.*
    """.trimIndent()
    }


    /**
     * Generar recomendaciones generales
     */
    private fun generateRecommendations(destination: String, numDays: Int): List<String> {
        return listOf(
            "Lleva ropa cómoda y calzado apropiado para caminar",
            "Verifica el clima antes de salir cada día",
            "Lleva efectivo, no todos los lugares aceptan tarjeta",
            "Descarga mapas offline por si pierdes conexión",
            "Prueba la comida local en mercados y pequeños restaurantes",
            "Respeta los horarios de los lugares turísticos",
            "Contrata un seguro de viaje para mayor tranquilidad"
        ).take(5)
    }

    // ========== HELPERS ==========

    private fun calculateDays(startDate: String, endDate: String): Int {
        return try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            if (start != null && end != null) {
                val diff = end.time - start.time
                (diff / (1000 * 60 * 60 * 24)).toInt() + 1
            } else 1
        } catch (e: Exception) {
            1
        }
    }

    private fun parseDate(dateStr: String): Calendar {
        val calendar = Calendar.getInstance()
        try {
            val date = dateFormat.parse(dateStr)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // Usar fecha actual si falla
        }
        return calendar
    }

    private fun formatDate(calendar: Calendar): String {
        return dateFormat.format(calendar.time)
    }

    private fun formatCurrency(amount: Double): String {
        return "$${String.format("%,.0f", amount)} COP"
    }
}