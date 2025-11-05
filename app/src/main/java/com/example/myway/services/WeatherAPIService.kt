package com.example.myway.services

import android.content.Context
import android.util.Log
import com.example.myway.ai.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


object WeatherAPIService {


    private const val API_KEY = "960b18aa002c4c708e2201647252710"
    private const val BASE_URL = "https://api.weatherapi.com/v1/current.json"
    private const val CACHE_DURATION_MS = 15 * 60 * 1000L // 15 minutos


    suspend fun getCurrentWeather(location: UserLocation, context: Context): String {
        return try {

            getCached(context, location)?.let { cached ->
                Log.d("WeatherAPI", "📦 Caché: $cached")
                return cached
            }


            if (!hasInternetConnection(context)) {
                Log.w("WeatherAPI", "⚠️ Sin internet, usando fallback")
                return getFallbackWeather()
            }


            val weather = fetchFromAPI(location)


            saveCache(context, location, weather)

            Log.d("WeatherAPI", "🌐 API: $weather para ${getCityName(location)}")
            weather

        } catch (e: Exception) {
            Log.e("WeatherAPI", "❌ Error: ${e.message}")
            getFallbackWeather()
        }
    }


    private suspend fun fetchFromAPI(location: UserLocation): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?key=$API_KEY&q=${location.latitude},${location.longitude}&lang=es"

                Log.d("WeatherAPI", "🌍 Consultando API...")
                val response = URL(url).readText()
                val json = JSONObject(response)

                // Extraer condición del clima
                val condition = json.getJSONObject("current")
                    .getJSONObject("condition")
                    .getString("text")
                    .lowercase()

                // Temperatura (opcional para logs)
                val tempC = json.getJSONObject("current").getDouble("temp_c")
                Log.d("WeatherAPI", "🌡️ Temperatura: ${tempC}°C")

                // Mapear a nuestros 3 estados
                mapWeatherCondition(condition)

            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error llamando API: ${e.message}")
                throw e
            }
        }
    }

    private fun mapWeatherCondition(condition: String): String {
        return when {

            condition.contains("despejado") ||
                    condition.contains("soleado") ||
                    condition.contains("claro") ||
                    condition.contains("sunny") ||
                    condition.contains("clear") -> "soleado"


            condition.contains("lluvia") ||
                    condition.contains("llovizna") ||
                    condition.contains("tormenta") ||
                    condition.contains("rain") ||
                    condition.contains("drizzle") ||
                    condition.contains("shower") ||
                    condition.contains("storm") ||
                    condition.contains("thunder") ||
                    condition.contains("chubasco") -> "lluvioso"


            else -> "nublado"
        }
    }


    private fun hasInternetConnection(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as android.net.ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }


    private fun getFallbackWeather(): String {
        return "nublado"
    }


    private fun getCityName(location: UserLocation): String {
        return when {
            location.latitude in 4.4..4.9 && location.longitude in -74.3..-73.9 -> "Bogotá"
            location.latitude in 6.0..6.4 && location.longitude in -75.7..-75.4 -> "Medellín"
            location.latitude in 3.3..3.6 && location.longitude in -76.7..-76.4 -> "Cali"
            location.latitude in 10.2..10.5 && location.longitude in -75.6..-75.4 -> "Cartagena"
            location.latitude in 10.8..11.2 && location.longitude in -75.0..-74.7 -> "Barranquilla"
            else -> "Ubicación (${location.latitude}, ${location.longitude})"
        }
    }

    // ========== CACHÉ ==========


    private fun getCached(context: Context, location: UserLocation): String? {
        return try {
            val prefs = context.getSharedPreferences("weather_api_cache", Context.MODE_PRIVATE)

            val cached = prefs.getString("weather", null) ?: return null
            val cachedLat = prefs.getFloat("lat", 0f).toDouble()
            val cachedLon = prefs.getFloat("lon", 0f).toDouble()
            val cachedTime = prefs.getLong("time", 0)


            val sameLocation = kotlin.math.abs(cachedLat - location.latitude) < 0.05 &&
                    kotlin.math.abs(cachedLon - location.longitude) < 0.05


            val elapsed = System.currentTimeMillis() - cachedTime
            val notExpired = elapsed < CACHE_DURATION_MS

            if (sameLocation && notExpired) {
                val minutesAgo = (elapsed / 1000 / 60).toInt()
                Log.d("WeatherAPI", "✅ Caché válido (hace $minutesAgo min)")
                cached
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WeatherAPI", "Error leyendo caché: ${e.message}")
            null
        }
    }


    private fun saveCache(context: Context, location: UserLocation, weather: String) {
        try {
            val prefs = context.getSharedPreferences("weather_api_cache", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("weather", weather)
                .putFloat("lat", location.latitude.toFloat())
                .putFloat("lon", location.longitude.toFloat())
                .putLong("time", System.currentTimeMillis())
                .apply()

            Log.d("WeatherAPI", "💾 Caché guardado: $weather")
        } catch (e: Exception) {
            Log.e("WeatherAPI", "Error guardando caché: ${e.message}")
        }
    }


    fun clearCache(context: Context) {
        try {
            val prefs = context.getSharedPreferences("weather_api_cache", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d("WeatherAPI", "🗑️ Caché limpiado")
        } catch (e: Exception) {
            Log.e("WeatherAPI", "Error limpiando caché: ${e.message}")
        }
    }
}