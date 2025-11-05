package com.example.myway.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AIService(private val context: Context) {


    private val apiKey = ""

    private val baseUrl = "https://api.openai.com/v1/chat/completions"
    private val model = "gpt-3.5-turbo"

    // ========== GENERAR ITINERARIO ==========

    suspend fun generateItinerary(request: ItineraryRequest): Result<ItineraryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Validar API Key
                if (apiKey.isEmpty()) {
                    return@withContext Result.failure(
                        Exception("API Key no configurada. Añade tu OpenAI API Key en BuildConfig.")
                    )
                }


                val prompt = buildItineraryPrompt(request)


                val response = callChatGPT(prompt, maxTokens = 2000)


                val itinerary = parseItineraryResponse(response, request)

                Result.success(itinerary)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ========== CONSTRUIR PROMPT OPTIMIZADO ==========

    private fun buildItineraryPrompt(request: ItineraryRequest): String {
        return """
            Crea un itinerario detallado de viaje con la siguiente información:
            
            **Destino:** ${request.destination}
            **Fechas:** ${request.startDate} a ${request.endDate}
            **Presupuesto:** ${request.budget}
            **Intereses:** ${request.interests.joinToString(", ")}
            **Estilo de viaje:** ${request.travelStyle}
            **Compañía:** ${request.companions}
            
            **Formato de respuesta (ESTRICTO):**
            
            # Itinerario: ${request.destination}
            
            ## Resumen
            [Breve descripción del viaje - máximo 2 líneas]
            
            ## Día 1: [Título del día]
            
            ### Mañana (9:00 - 12:00)
            - **Actividad:** [Nombre]
            - **Descripción:** [1-2 líneas]
            - **Ubicación:** [Dirección aproximada]
            - **Costo estimado:** $[cantidad]
            - **Duración:** [tiempo]
            
            ### Tarde (14:00 - 18:00)
            [Misma estructura]
            
            ### Noche (19:00 - 22:00)
            [Misma estructura]
            
            ### Comidas recomendadas:
            - **Desayuno:** [Lugar] - $[costo]
            - **Almuerzo:** [Lugar] - $[costo]
            - **Cena:** [Lugar] - $[costo]
            
            [Repetir estructura para cada día]
            
            ## Costo total estimado
            [Desglose por categorías: transporte, alojamiento, comidas, actividades]
            
            ## Recomendaciones adicionales
            - [3-5 tips específicos para el destino]
            
            **IMPORTANTE:**
            - Sé conciso y específico
            - Incluye precios realistas en COP (pesos colombianos)
            - Considera el presupuesto: ${request.budget}
            - Adapta actividades a: ${request.travelStyle}
            - Incluye lugares apropiados para: ${request.companions}
            - No excedas 2000 tokens en tu respuesta
        """.trimIndent()
    }

    // ========== LLAMADA A CHATGPT API ==========

    private suspend fun callChatGPT(
        prompt: String,
        maxTokens: Int = 2000
    ): String {
        return withContext(Dispatchers.IO) {
            val url = URL(baseUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    doOutput = true
                }

                // Construir request body
                val requestBody = JSONObject().apply {
                    put("model", model)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "Eres un experto planificador de viajes. Creas itinerarios detallados, prácticos y realistas.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("max_tokens", maxTokens)
                    put("temperature", 0.7)
                }

                // Enviar request
                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray())
                }

                // Leer respuesta
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    throw Exception("Error API: $responseCode - $errorBody")
                }

            } finally {
                connection.disconnect()
            }
        }
    }

    // ========== PARSEAR RESPUESTA ==========

    private fun parseItineraryResponse(
        jsonResponse: String,
        request: ItineraryRequest
    ): ItineraryResponse {
        val json = JSONObject(jsonResponse)


        val content = json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")


        val dayPlans = parseDayPlans(content)


        val estimatedCost = extractEstimatedCost(content)


        val recommendations = extractRecommendations(content)

        return ItineraryResponse(
            itinerary = content,
            dayByDay = dayPlans,
            estimatedCost = estimatedCost,
            recommendations = recommendations,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parseDayPlans(content: String): List<DayPlan> {
        val dayPlans = mutableListOf<DayPlan>()


        val dayPattern = """## Día (\d+): (.+)""".toRegex()
        val matches = dayPattern.findAll(content)

        matches.forEach { match ->
            val dayNumber = match.groupValues[1].toIntOrNull() ?: 0
            val dayTitle = match.groupValues[2].trim()


            val activities = extractActivities(content, dayNumber)
            val meals = extractMeals(content, dayNumber)

            dayPlans.add(
                DayPlan(
                    day = dayNumber,
                    title = dayTitle,
                    activities = activities,
                    meals = meals
                )
            )
        }

        return dayPlans
    }

    private fun extractActivities(content: String, day: Int): List<Activity> {

        return listOf(
            Activity(
                time = "Mañana",
                name = "Actividad matutina",
                description = "Descripción pendiente de parseo",
                location = null,
                estimatedCost = null,
                duration = "3 horas"
            )
        )
    }

    private fun extractMeals(content: String, day: Int): List<Meal> {
        return listOf(
            Meal(
                type = "desayuno",
                recommendation = "Por definir",
                estimatedCost = "$20.000"
            )
        )
    }

    private fun extractEstimatedCost(content: String): String {
        val costPattern = """Costo total estimado[:\s]+\$?([\d.,]+)""".toRegex(RegexOption.IGNORE_CASE)
        val match = costPattern.find(content)
        return match?.groupValues?.get(1) ?: "No especificado"
    }

    private fun extractRecommendations(content: String): List<String> {
        val recommendations = mutableListOf<String>()


        val recSection = """## Recomendaciones adicionales(.+?)(?=##|$)""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = recSection.find(content)

        match?.groupValues?.get(1)?.let { section ->

            val bulletPattern = """[-*]\s+(.+)""".toRegex()
            bulletPattern.findAll(section).forEach {
                recommendations.add(it.groupValues[1].trim())
            }
        }

        return recommendations
    }

    // ========== CACHÉ DE ITINERARIOS ==========

    suspend fun getCachedItinerary(userId: String, destination: String): ItineraryResponse? {
        return try {
            // Implementar caché en Firestore para evitar llamadas repetidas
            // y ahorrar costos de API
            null // Por implementar
        } catch (e: Exception) {
            null
        }
    }

    suspend fun cacheItinerary(
        userId: String,
        destination: String,
        itinerary: ItineraryResponse
    ) {
        try {
            // Guardar en Firestore para reutilizar
            // Por implementar
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ========== ESTIMACIÓN DE COSTOS ==========

    fun estimateTokenCost(prompt: String): Int {
        // Estimación aproximada: 1 token ≈ 4 caracteres
        return (prompt.length / 4) + 2000 // Prompt + respuesta esperada
    }

    fun estimateApiCost(tokens: Int): Double {
        // gpt-3.5-turbo: ~$0.002 por 1K tokens
        return (tokens / 1000.0) * 0.002
    }
}