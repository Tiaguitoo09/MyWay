package com.example.myway.ai

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utilidad para poblar Firebase con lugares curados de Bogotá
 */
object PopulatePlaces {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Añade 20 lugares emblemáticos de Bogotá con tags perfectos
     */
    suspend fun addSamplePlaces() {
        val lugares = listOf(
            // RESTAURANTES PREMIUM
            Place(
                id = "andres_carne_res",
                name = "Andrés Carne de Res",
                address = "Cl. 3 #11a-56, Chía",
                latitude = 4.8611,
                longitude = -74.0581,
                photoUrl = null,
                category = "restaurante",
                priceLevel = 3,
                rating = 4.5,
                tags = listOf("vibrante", "colombiano", "familiar", "festivo", "único", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "criterio",
                name = "Criterión",
                address = "Cra 13 #93B-30, Bogotá",
                latitude = 4.6764,
                longitude = -74.0516,
                photoUrl = null,
                category = "restaurante",
                priceLevel = 4,
                rating = 4.7,
                tags = listOf("romántico", "elegante", "gourmet", "premium", "íntimo", "excelente"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "leo_cocina_vino",
                name = "Leo Cocina y Cava",
                address = "Cra 7 #71-21, Bogotá",
                latitude = 4.6538,
                longitude = -74.0613,
                photoUrl = null,
                category = "restaurante",
                priceLevel = 4,
                rating = 4.6,
                tags = listOf("romántico", "gourmet", "elegante", "premium", "internacional", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // RESTAURANTES MODERADOS
            Place(
                id = "wok",
                name = "Wok",
                address = "Cra 13 #85-74, Bogotá",
                latitude = 4.6699,
                longitude = -74.0543,
                photoUrl = null,
                category = "restaurante",
                priceLevel = 2,
                rating = 4.3,
                tags = listOf("asiático", "moderno", "social", "bueno", "familiar"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "crepes_waffles",
                name = "Crepes & Waffles",
                address = "Cra 15 #82-58, Bogotá",
                latitude = 4.6654,
                longitude = -74.0552,
                photoUrl = null,
                category = "restaurante",
                priceLevel = 2,
                rating = 4.4,
                tags = listOf("familiar", "acogedor", "postres", "bueno", "popular"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // CAFÉS
            Place(
                id = "azahar_cafe",
                name = "Azahar Café",
                address = "Cra 4A #57-52, Bogotá",
                latitude = 4.6396,
                longitude = -74.0631,
                photoUrl = null,
                category = "cafe",
                priceLevel = 2,
                rating = 4.6,
                tags = listOf("acogedor", "café especial", "tranquilo", "artesanal", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "cafe_cultor",
                name = "Café Cultor",
                address = "Cra 7 #63-82, Bogotá",
                latitude = 4.6474,
                longitude = -74.0620,
                photoUrl = null,
                category = "cafe",
                priceLevel = 2,
                rating = 4.5,
                tags = listOf("acogedor", "café especial", "tranquilo", "moderno", "bueno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "juan_valdez_centro",
                name = "Juan Valdez Café",
                address = "Cra 7 #32-29, Bogotá",
                latitude = 4.6116,
                longitude = -74.0701,
                photoUrl = null,
                category = "cafe",
                priceLevel = 1,
                rating = 4.2,
                tags = listOf("colombiano", "café", "económico", "popular"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // PARQUES
            Place(
                id = "parque_93",
                name = "Parque 93",
                address = "Cra 13 #93A-40, Bogotá",
                latitude = 4.6772,
                longitude = -74.0517,
                photoUrl = null,
                category = "parque",
                priceLevel = 0,
                rating = 4.5,
                tags = listOf("natural", "familiar", "aire libre", "social", "popular", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado")
            ),
            Place(
                id = "parque_simon_bolivar",
                name = "Parque Simón Bolívar",
                address = "Calle 63 con Carrera 68, Bogotá",
                latitude = 4.6555,
                longitude = -74.0927,
                photoUrl = null,
                category = "parque",
                priceLevel = 0,
                rating = 4.6,
                tags = listOf("natural", "amplio", "familiar", "aire libre", "deportivo", "excelente"),
                weatherSuitable = listOf("soleado", "nublado")
            ),
            Place(
                id = "jardin_botanico",
                name = "Jardín Botánico",
                address = "Calle 63 #68-95, Bogotá",
                latitude = 4.6699,
                longitude = -74.0997,
                photoUrl = null,
                category = "parque",
                priceLevel = 1,
                rating = 4.7,
                tags = listOf("natural", "educativo", "familiar", "tranquilo", "cultural", "excelente"),
                weatherSuitable = listOf("soleado", "nublado")
            ),

            // MUSEOS
            Place(
                id = "museo_oro",
                name = "Museo del Oro",
                address = "Cra 6 #15-88, Bogotá",
                latitude = 4.6016,
                longitude = -74.0726,
                photoUrl = null,
                category = "museo",
                priceLevel = 1,
                rating = 4.8,
                tags = listOf("cultural", "histórico", "educativo", "imperdible", "excelente"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "museo_botero",
                name = "Museo Botero",
                address = "Cl. 11 #4-41, Bogotá",
                latitude = 4.5971,
                longitude = -74.0745,
                photoUrl = null,
                category = "museo",
                priceLevel = 0,
                rating = 4.7,
                tags = listOf("cultural", "arte", "gratuito", "histórico", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // BARES/VIDA NOCTURNA
            Place(
                id = "theatron",
                name = "Theatron",
                address = "Calle 58 #10-32, Bogotá",
                latitude = 4.6412,
                longitude = -74.0654,
                photoUrl = null,
                category = "bar",
                priceLevel = 2,
                rating = 4.3,
                tags = listOf("nocturno", "diverso", "social", "vibrante", "discoteca"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "armando_records",
                name = "Armando Records",
                address = "Cra 14 #82-16, Bogotá",
                latitude = 4.6657,
                longitude = -74.0551,
                photoUrl = null,
                category = "bar",
                priceLevel = 2,
                rating = 4.4,
                tags = listOf("nocturno", "música en vivo", "bohemio", "social", "bueno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // CENTROS COMERCIALES
            Place(
                id = "centro_andino",
                name = "Centro Comercial Andino",
                address = "Cra 11 #82-71, Bogotá",
                latitude = 4.6652,
                longitude = -74.0546,
                photoUrl = null,
                category = "centro_comercial",
                priceLevel = 3,
                rating = 4.5,
                tags = listOf("shopping", "premium", "entretenimiento", "moderno", "popular"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "unicentro",
                name = "Unicentro",
                address = "Av. Cra 15 #123-30, Bogotá",
                latitude = 4.7010,
                longitude = -74.0398,
                photoUrl = null,
                category = "centro_comercial",
                priceLevel = 2,
                rating = 4.3,
                tags = listOf("shopping", "familiar", "entretenimiento", "amplio", "bueno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // LUGARES ÚNICOS
            Place(
                id = "monserrate",
                name = "Cerro de Monserrate",
                address = "Vía al Cerro, Bogotá",
                latitude = 4.6056,
                longitude = -74.0565,
                photoUrl = null,
                category = "mirador",
                priceLevel = 1,
                rating = 4.7,
                tags = listOf("mirador", "religioso", "turístico", "aventura", "imperdible", "excelente"),
                weatherSuitable = listOf("soleado", "nublado")
            ),
            Place(
                id = "usaquen",
                name = "Mercado de las Pulgas - Usaquén",
                address = "Calle 119 con Carrera 6, Bogotá",
                latitude = 4.6988,
                longitude = -74.0316,
                photoUrl = null,
                category = "mercado",
                priceLevel = 2,
                rating = 4.5,
                tags = listOf("artesanal", "familiar", "cultural", "gastronómico", "domingo", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado")
            ),
            Place(
                id = "zona_t",
                name = "Zona T",
                address = "Calle 82 con Carrera 13, Bogotá",
                latitude = 4.6657,
                longitude = -74.0548,
                photoUrl = null,
                category = "zona_comercial",
                priceLevel = 3,
                rating = 4.4,
                tags = listOf("social", "gastronómico", "nocturno", "shopping", "vibrante", "popular"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            )
        )

        try {
            val batch = firestore.batch()

            lugares.forEach { lugar ->
                val docRef = firestore.collection("lugares").document(lugar.id)
                val data = hashMapOf(
                    "name" to lugar.name,
                    "address" to lugar.address,
                    "latitude" to lugar.latitude,
                    "longitude" to lugar.longitude,
                    "photoUrl" to lugar.photoUrl,
                    "category" to lugar.category,
                    "priceLevel" to lugar.priceLevel,
                    "rating" to lugar.rating,
                    "tags" to lugar.tags,
                    "weatherSuitable" to lugar.weatherSuitable
                )
                batch.set(docRef, data)
            }

            batch.commit().await()
            Log.d("PopulatePlaces", "✅ ${lugares.size} lugares añadidos exitosamente")

        } catch (e: Exception) {
            Log.e("PopulatePlaces", "❌ Error: ${e.message}", e)
            throw e
        }
    }

    /**
     * Limpia todos los lugares de Firebase (útil para testing)
     */
    suspend fun clearAllPlaces() {
        try {
            val snapshot = firestore.collection("lugares").get().await()
            val batch = firestore.batch()

            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Log.d("PopulatePlaces", "🗑️ Lugares eliminados")

        } catch (e: Exception) {
            Log.e("PopulatePlaces", "❌ Error limpiando: ${e.message}")
        }
    }
}