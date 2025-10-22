package com.example.myway.data.repository

import android.util.Log
import com.example.myway.data.models.RecentPlace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecentPlacesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        // ✅ AHORA ES UNA COLECCIÓN RAÍZ SEPARADA
        private const val COLLECTION_RECIENTES = "recientes"
        private const val MAX_RECIENTES = 10
        private const val TAG = "RecentPlacesRepo"
    }

    /**
     * Guarda un lugar reciente para el usuario actual
     * Estructura: recientes/{userId}/{placeId}
     */
    suspend fun saveRecentPlace(
        placeId: String,
        placeName: String,
        placeAddress: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid

            Log.d(TAG, "🔑 UserID actual: $userId")

            if (userId == null) {
                Log.e(TAG, "❌ Usuario NO autenticado")
                return Result.failure(Exception("Usuario no autenticado"))
            }

            Log.d(TAG, "📍 Guardando lugar: $placeName para usuario: $userId")

            val recentPlace = RecentPlace(
                id = placeId,
                name = placeName,
                address = placeAddress,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis()
            )

            // ✅ NUEVA RUTA: recientes/{userId}/{placeId}
            val docRef = db.collection(COLLECTION_RECIENTES)
                .document(userId)
                .collection("lugares")
                .document(placeId)

            Log.d(TAG, "📝 Ruta Firebase: ${docRef.path}")

            docRef.set(recentPlace.toMap()).await()

            Log.d(TAG, "✅ Documento guardado exitosamente")

            // Limpiar lugares antiguos
            cleanOldRecents(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar lugar reciente: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene los lugares recientes en tiempo real
     * Lee de: recientes/{userId}/lugares
     */
    fun getRecentPlacesFlow(): Flow<List<RecentPlace>> = callbackFlow {
        val userId = auth.currentUser?.uid

        Log.d(TAG, "🔄 Iniciando Flow para userId: $userId")

        if (userId == null) {
            Log.e(TAG, "❌ No hay usuario autenticado para Flow")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // ✅ NUEVA RUTA: recientes/{userId}/lugares
        val collectionRef = db.collection(COLLECTION_RECIENTES)
            .document(userId)
            .collection("lugares")

        Log.d(TAG, "📂 Escuchando colección: ${collectionRef.path}")

        val listener = collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(MAX_RECIENTES.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar cambios: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "⚠️ Snapshot es null")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "📦 Documentos recibidos: ${snapshot.documents.size}")

                val recientes = snapshot.documents.mapNotNull { doc ->
                    try {
                        Log.d(TAG, "📄 Procesando documento: ${doc.id}")
                        doc.data?.let { data ->
                            Log.d(TAG, "📝 Datos: $data")
                            RecentPlace.fromMap(data)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al parsear documento ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                Log.d(TAG, "✅ Recientes procesados: ${recientes.size}")
                trySend(recientes)
            }

        awaitClose {
            Log.d(TAG, "🔌 Cerrando listener de recientes")
            listener.remove()
        }
    }

    /**
     * Elimina los lugares más antiguos si hay más del máximo permitido
     */
    private suspend fun cleanOldRecents(userId: String) {
        try {
            val snapshot = db.collection(COLLECTION_RECIENTES)
                .document(userId)
                .collection("lugares")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val total = snapshot.documents.size
            Log.d(TAG, "🧹 Total de recientes: $total (máximo: $MAX_RECIENTES)")

            if (total > MAX_RECIENTES) {
                val toDelete = snapshot.documents.drop(MAX_RECIENTES)
                Log.d(TAG, "🗑️ Eliminando ${toDelete.size} lugares antiguos")

                toDelete.forEach { doc ->
                    Log.d(TAG, "🗑️ Eliminando: ${doc.id}")
                    doc.reference.delete().await()
                }

                Log.d(TAG, "✅ Limpieza completada")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al limpiar lugares antiguos: ${e.message}", e)
        }
    }

    /**
     * Limpia todos los lugares recientes del usuario
     */
    suspend fun clearAllRecents(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            Log.d(TAG, "🗑️ Limpiando todos los recientes del usuario: $userId")

            val snapshot = db.collection(COLLECTION_RECIENTES)
                .document(userId)
                .collection("lugares")
                .get()
                .await()

            Log.d(TAG, "🗑️ Encontrados ${snapshot.documents.size} documentos para eliminar")

            snapshot.documents.forEach { doc ->
                Log.d(TAG, "🗑️ Eliminando: ${doc.id}")
                doc.reference.delete().await()
            }

            Log.d(TAG, "✅ Todos los recientes eliminados")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al limpiar todos los recientes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene recientes de un usuario específico (útil para admin)
     */
    suspend fun getRecentPlacesByUserId(userId: String): Result<List<RecentPlace>> {
        return try {
            val snapshot = db.collection(COLLECTION_RECIENTES)
                .document(userId)
                .collection("lugares")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_RECIENTES.toLong())
                .get()
                .await()

            val recientes = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.let { RecentPlace.fromMap(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al parsear documento", e)
                    null
                }
            }

            Log.d(TAG, "📍 Recientes del usuario $userId: ${recientes.size}")
            Result.success(recientes)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener recientes", e)
            Result.failure(e)
        }
    }
}