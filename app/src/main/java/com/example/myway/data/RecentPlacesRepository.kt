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
        private const val COLLECTION_USUARIOS = "usuarios"
        private const val COLLECTION_RECIENTES = "recientes"
        private const val MAX_RECIENTES = 10
        private const val TAG = "RecentPlacesRepo"
    }

    /**
     * Guarda un lugar reciente
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
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val recentPlace = RecentPlace(
                id = placeId,
                name = placeName,
                address = placeAddress,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis()
            )

            db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .collection(COLLECTION_RECIENTES)
                .document(placeId)
                .set(recentPlace.toMap())
                .await()

            cleanOldRecents(userId)

            Log.d(TAG, "✅ Lugar reciente guardado: $placeName")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar lugar reciente", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene lugares recientes en tiempo real
     */
    fun getRecentPlacesFlow(): Flow<List<RecentPlace>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(COLLECTION_USUARIOS)
            .document(userId)
            .collection(COLLECTION_RECIENTES)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(MAX_RECIENTES.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar cambios", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val recientes = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.data?.let { RecentPlace.fromMap(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al parsear documento", e)
                        null
                    }
                } ?: emptyList()

                trySend(recientes)
                Log.d(TAG, "📍 Recientes actualizados: ${recientes.size}")
            }

        awaitClose { listener.remove() }
    }

    /**
     * Limpia lugares antiguos
     */
    private suspend fun cleanOldRecents(userId: String) {
        try {
            val snapshot = db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .collection(COLLECTION_RECIENTES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            if (snapshot.documents.size > MAX_RECIENTES) {
                val toDelete = snapshot.documents.drop(MAX_RECIENTES)
                toDelete.forEach { doc ->
                    doc.reference.delete().await()
                }
                Log.d(TAG, "🗑️ Limpiados ${toDelete.size} lugares antiguos")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al limpiar lugares antiguos", e)
        }
    }

    /**
     * Limpia todos los recientes
     */
    suspend fun clearAllRecents(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val snapshot = db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .collection(COLLECTION_RECIENTES)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            Log.d(TAG, "🗑️ Todos los recientes eliminados")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al limpiar recientes", e)
            Result.failure(e)
        }
    }
}