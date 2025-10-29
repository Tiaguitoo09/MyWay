package com.example.myway.ai

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object PopulatePlaces {



    private val firestore = FirebaseFirestore.getInstance()


    private fun fixUnsplashUrl(url: String?): String? {
        return url?.let {
            if (it.contains("unsplash.com")) {
                // Agregar parámetros necesarios para que funcione
                "$it&auto=format&q=80"
            } else {
                it
            }
        }
    }

    suspend fun addSamplePlaces() {
        val lugares = listOf(
            // ========== CAFÉS ==========
            Place(
                id = "azahar_cafe",
                name = "Azahar Café",
                address = "Cra 4A #57-52, Bogotá",
                latitude = 4.6396,
                longitude = -74.0631,
                photoUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&h=600&fit=crop",
                category = "cafe",
                priceLevel = 1,
                rating = 4.2,
                tags = listOf("colombiano", "café", "económico", "popular"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== RESTAURANTES PREMIUM ==========
            Place(
                id = "andres_carne_res",
                name = "Andrés Carne de Res",
                address = "Cl. 3 #11a-56, Chía",
                latitude = 4.8611,
                longitude = -74.0581,
                photoUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&h=600&fit=crop",
                category = "restaurante",
                priceLevel = 3,
                rating = 4.5,
                tags = listOf("vibrante", "colombiano", "familiar", "festivo", "único", "recomendado", "nocturno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "criterio",
                name = "Criterión",
                address = "Cra 13 #93B-30, Bogotá",
                latitude = 4.6764,
                longitude = -74.0516,
                photoUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&h=600&fit=crop",
                category = "restaurante",
                priceLevel = 4,
                rating = 4.7,
                tags = listOf("romántico", "elegante", "gourmet", "premium", "íntimo", "excelente", "nocturno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "leo_cocina_vino",
                name = "Leo Cocina y Cava",
                address = "Cra 7 #71-21, Bogotá",
                latitude = 4.6538,
                longitude = -74.0613,
                photoUrl = "https://images.unsplash.com/photo-1424847651672-bf20a4b0982b?w=800&h=600&fit=crop",
                category = "restaurante",
                priceLevel = 4,
                rating = 4.6,
                tags = listOf("romántico", "gourmet", "elegante", "premium", "internacional", "recomendado", "nocturno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== RESTAURANTES MODERADOS ==========
            Place(
                id = "wok",
                name = "Wok",
                address = "Cra 13 #85-74, Bogotá",
                latitude = 4.6699,
                longitude = -74.0543,
                photoUrl = "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&h=600&fit=crop",
                category = "restaurante",
                priceLevel = 2,
                rating = 4.4,
                tags = listOf("familiar", "acogedor", "postres", "bueno", "popular"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== PARQUES ==========
            Place(
                id = "parque_93",
                name = "Parque 93",
                address = "Cra 13 #93A-40, Bogotá",
                latitude = 4.6772,
                longitude = -74.0517,
                photoUrl = "https://images.unsplash.com/photo-1519331379826-f10be5486c6f?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1466611653911-95081537e5b7?w=800&h=600&fit=crop",
                category = "parque",
                priceLevel = 1,
                rating = 4.7,
                tags = listOf("natural", "educativo", "familiar", "tranquilo", "cultural", "excelente"),
                weatherSuitable = listOf("soleado", "nublado")
            ),

            // ========== MUSEOS ==========
            Place(
                id = "museo_oro",
                name = "Museo del Oro",
                address = "Cra 6 #15-88, Bogotá",
                latitude = 4.6016,
                longitude = -74.0726,
                photoUrl = "https://images.unsplash.com/photo-1565352051603-3b21e5c15e87?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1564399579883-451a5d44ec08?w=800&h=600&fit=crop",
                category = "museo",
                priceLevel = 0,
                rating = 4.7,
                tags = listOf("cultural", "arte", "gratuito", "histórico", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== BARES/VIDA NOCTURNA ==========
            Place(
                id = "theatron",
                name = "Theatron",
                address = "Calle 58 #10-32, Bogotá",
                latitude = 4.6412,
                longitude = -74.0654,
                photoUrl = "https://images.unsplash.com/photo-1566737236500-c8ac43014a67?w=800&h=600&fit=crop",
                category = "discoteca",
                priceLevel = 2,
                rating = 4.3,
                tags = listOf("nocturno", "diverso", "social", "vibrante", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "armando_records",
                name = "Armando Records",
                address = "Cra 14 #82-16, Bogotá",
                latitude = 4.6657,
                longitude = -74.0551,
                photoUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&h=600&fit=crop",
                category = "bar",
                priceLevel = 2,
                rating = 4.4,
                tags = listOf("nocturno", "música en vivo", "bohemio", "social", "bueno", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "el_mozo",
                name = "El Mozo",
                address = "Cra 5 #66A-16, Bogotá",
                latitude = 4.6489,
                longitude = -74.0623,
                photoUrl = "https://images.unsplash.com/photo-1543007630-9710e4a00a20?w=800&h=600&fit=crop",
                category = "bar",
                priceLevel = 2,
                rating = 4.5,
                tags = listOf("nocturno", "cerveza artesanal", "social", "moderno", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "video_club",
                name = "Video Club",
                address = "Calle 93B #11A-27, Bogotá",
                latitude = 4.6771,
                longitude = -74.0514,
                photoUrl = "https://images.unsplash.com/photo-1571266028243-d220c6e0fedb?w=800&h=600&fit=crop",
                category = "bar",
                priceLevel = 3,
                rating = 4.6,
                tags = listOf("nocturno", "moderno", "premium", "social", "vibrante", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== CENTROS COMERCIALES ==========
            Place(
                id = "centro_andino",
                name = "Centro Comercial Andino",
                address = "Cra 11 #82-71, Bogotá",
                latitude = 4.6652,
                longitude = -74.0546,
                photoUrl = "https://images.unsplash.com/photo-1519167758481-83f29da8785a?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1555529902-5261145633bf?w=800&h=600&fit=crop",
                category = "centro_comercial",
                priceLevel = 2,
                rating = 4.3,
                tags = listOf("shopping", "familiar", "entretenimiento", "amplio", "bueno"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== LUGARES ÚNICOS ==========
            Place(
                id = "monserrate",
                name = "Cerro de Monserrate",
                address = "Vía al Cerro, Bogotá",
                latitude = 4.6056,
                longitude = -74.0565,
                photoUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
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
                photoUrl = "https://images.unsplash.com/photo-1555529669-2269763671c0?w=800&h=600&fit=crop",
                category = "mercado",
                priceLevel = 2,
                rating = 4.5,
                tags = listOf("artesanal", "familiar", "cultural", "gastronómico", "fin de semana", "recomendado"),
                weatherSuitable = listOf("soleado", "nublado")
            ),
            Place(
                id = "zona_t",
                name = "Zona T",
                address = "Calle 82 con Carrera 13, Bogotá",
                latitude = 4.6657,
                longitude = -74.0548,
                photoUrl = "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=800&h=600&fit=crop",
                category = "zona_comercial",
                priceLevel = 3,
                rating = 4.4,
                tags = listOf("social", "gastronómico", "nocturno", "shopping", "vibrante", "popular", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),

            // ========== NUEVOS LUGARES NOCTURNOS ==========
            Place(
                id = "baum_bar",
                name = "Baum Bar",
                address = "Calle 85 #12-18, Bogotá",
                latitude = 4.6688,
                longitude = -74.0540,
                photoUrl = "https://images.unsplash.com/photo-1572116469696-31de0f17cc34?w=800&h=600&fit=crop",
                category = "bar",
                priceLevel = 3,
                rating = 4.7,
                tags = listOf("nocturno", "cocktails", "premium", "romántico", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            ),
            Place(
                id = "octava",
                name = "Octava",
                address = "Calle 85 #13-31, Bogotá",
                latitude = 4.6693,
                longitude = -74.0536,
                photoUrl = "https://images.unsplash.com/photo-1470337458703-46ad1756a187?w=800&h=600&fit=crop",
                category = "discoteca",
                priceLevel = 3,
                rating = 4.5,
                tags = listOf("nocturno", "electrónica", "vibrante", "premium", "fin de semana"),
                weatherSuitable = listOf("soleado", "nublado", "lluvioso")
            )
        )

        try {
            // 🔥 IMPORTANTE: Limpiar primero para evitar duplicados
            Log.d("PopulatePlaces", "🗑️ Limpiando lugares anteriores...")
            clearAllPlaces()

            val batch = firestore.batch()

            lugares.forEach { lugar ->
                val docRef = firestore.collection("lugares").document(lugar.id)
                val data = hashMapOf(
                    "name" to lugar.name,
                    "address" to lugar.address,
                    "latitude" to lugar.latitude,
                    "longitude" to lugar.longitude,
                    "photoUrl" to fixUnsplashUrl(lugar.photoUrl), // ✅ APLICAR FIX
                    "category" to lugar.category,
                    "priceLevel" to lugar.priceLevel,
                    "rating" to lugar.rating,
                    "tags" to lugar.tags,
                    "weatherSuitable" to lugar.weatherSuitable
                )
                batch.set(docRef, data)

                // Log para verificar
                Log.d("PopulatePlaces", "✅ ${lugar.name}: ${fixUnsplashUrl(lugar.photoUrl)}")
            }

            batch.commit().await()
            Log.d("PopulatePlaces", "✅ ${lugares.size} lugares añadidos exitosamente con URLs actualizadas")

        } catch (e: Exception) {
            Log.e("PopulatePlaces", "❌ Error: ${e.message}", e)
            throw e
        }
    }

    suspend fun clearAllPlaces() {
        try {
            val snapshot = firestore.collection("lugares").get().await()
            val batch = firestore.batch()

            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Log.d("PopulatePlaces", "🗑️ ${snapshot.documents.size} lugares eliminados")

        } catch (e: Exception) {
            Log.e("PopulatePlaces", "❌ Error limpiando: ${e.message}")
        }
    }
}