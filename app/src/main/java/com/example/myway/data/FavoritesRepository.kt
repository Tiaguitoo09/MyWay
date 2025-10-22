package com.example.myway.data

import android.content.Context
import com.example.myway.BuildConfig
import com.example.myway.data.local.AppDatabase
import com.example.myway.data.local.FavoritePlaceEntity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.favoritePlaceDao()

    private val placesClient = if (!Places.isInitialized()) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        Places.createClient(context)
    } else {
        Places.createClient(context)
    }

    // Obtener todos los favoritos
    fun getAllFavorites() = dao.getAllFavorites()

    // Verificar si es favorito
    suspend fun isFavorite(placeId: String) = dao.isFavorite(placeId)

    // Guardar lugar en favoritos obteniendo datos de Google Places
    suspend fun saveFavorite(placeId: String, placeName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val placeFields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.PHOTO_METADATAS
                )

                val request = FetchPlaceRequest.newInstance(placeId, placeFields)
                val response = placesClient.fetchPlace(request).await()
                val place = response.place

                val photoUrl = place.photoMetadatas?.firstOrNull()?.let { photoMetadata ->
                    null
                }

                val favoritePlace = FavoritePlaceEntity(
                    id = place.id ?: placeId,
                    name = place.name ?: placeName,
                    address = place.address,
                    photoUrl = photoUrl,
                    latitude = place.latLng?.latitude ?: 0.0,
                    longitude = place.latLng?.longitude ?: 0.0,
                    timestamp = System.currentTimeMillis()
                )

                dao.insertFavorite(favoritePlace)
                Result.success(Unit)

            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    // Eliminar favorito
    suspend fun deleteFavorite(placeId: String) {
        withContext(Dispatchers.IO) {
            dao.deleteFavoriteById(placeId)
        }
    }

    // Buscar favoritos
    fun searchFavorites(query: String) = dao.searchFavorites(query)
}