package com.example.myway.data

import android.content.Context
import com.example.myway.BuildConfig
import com.example.myway.data.local.AppDatabase
import com.example.myway.data.local.FavoritePlaceEntity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository(private val context: Context) {

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

                // 🆕 Obtener URL de la foto correctamente
                val photoUrl = place.photoMetadatas?.firstOrNull()?.let { photoMetadata ->
                    try {
                        // Crear request para la foto con ancho máximo de 400px
                        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(400)
                            .setMaxHeight(400)
                            .build()

                        val photoResponse = placesClient.fetchPhoto(photoRequest).await()

                        // Guardar la imagen en caché y retornar la URL
                        val bitmap = photoResponse.bitmap

                        // Guardar bitmap en almacenamiento interno y retornar la ruta
                        val filename = "place_${placeId}.jpg"
                        context.openFileOutput(filename, Context.MODE_PRIVATE).use { output ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, output)
                        }

                        // Retornar la ruta del archivo
                        context.filesDir.absolutePath + "/" + filename

                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
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

    // Eliminar favorito (también eliminar la imagen si existe)
    suspend fun deleteFavorite(placeId: String) {
        withContext(Dispatchers.IO) {
            // Eliminar imagen del almacenamiento si existe
            try {
                val filename = "place_${placeId}.jpg"
                context.deleteFile(filename)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            dao.deleteFavoriteById(placeId)
        }
    }

    // Buscar favoritos
    fun searchFavorites(query: String) = dao.searchFavorites(query)
}