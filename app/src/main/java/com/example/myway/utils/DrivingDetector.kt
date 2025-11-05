package com.example.myway.utils

import android.content.Context
import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs


object DrivingDetector {

    // Estados observables
    private val _isDriving = MutableStateFlow(false)
    val isDriving: StateFlow<Boolean> = _isDriving.asStateFlow()

    // Configuración de detección
    private const val DRIVING_SPEED_THRESHOLD = 5.0 // m/s (~18 km/h)
    private const val HIGH_SPEED_THRESHOLD = 15.0 // m/s (~54 km/h)
    private const val ACCELERATION_THRESHOLD = 2.0 // m/s²

    // Historial de ubicaciones
    private var lastLocation: Location? = null
    private var lastUpdateTime: Long = 0L
    private var lastSpeed: Float = 0f

    // Contador de detecciones consecutivas
    private var drivingDetectionCount = 0
    private var stoppedDetectionCount = 0
    private const val DETECTION_THRESHOLD = 3


    private var context: Context? = null


    fun initialize(appContext: Context) {
        context = appContext.applicationContext
    }

    /**
     * Actualiza la ubicación y analiza si está conduciendo
     */
    fun updateLocation(location: Location) {

        val modoCopiloto = context?.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
            ?.getBoolean("modo_copiloto", false) ?: false

        // Si modo copiloto está ON, siempre marcar como NO conduciendo
        if (modoCopiloto) {
            _isDriving.value = false
            drivingDetectionCount = 0
            stoppedDetectionCount = 0
            return
        }


        val currentTime = System.currentTimeMillis()

        lastLocation?.let { last ->
            // Calcular tiempo transcurrido
            val timeDelta = (currentTime - lastUpdateTime) / 1000.0 // segundos

            if (timeDelta > 0.5 && timeDelta < 10) { // Entre 0.5 y 10 segundos
                // Calcular velocidad instantánea
                val distance = last.distanceTo(location)
                val speed = distance / timeDelta.toFloat()

                // Calcular aceleración
                val acceleration = abs(speed - lastSpeed) / timeDelta.toFloat()

                // Lógica de detección
                val isProbablyDriving = speed > DRIVING_SPEED_THRESHOLD &&
                        (speed > HIGH_SPEED_THRESHOLD || acceleration > ACCELERATION_THRESHOLD)

                if (isProbablyDriving) {
                    drivingDetectionCount++
                    stoppedDetectionCount = 0

                    // Activar estado de conducción tras detecciones consecutivas
                    if (drivingDetectionCount >= DETECTION_THRESHOLD) {
                        _isDriving.value = true
                    }
                } else {
                    stoppedDetectionCount++
                    drivingDetectionCount = 0

                    // Desactivar estado de conducción tras detecciones de parada
                    if (stoppedDetectionCount >= DETECTION_THRESHOLD) {
                        _isDriving.value = false
                    }
                }

                lastSpeed = speed
            }
        }

        lastLocation = location
        lastUpdateTime = currentTime
    }


    fun canUseAppFreely(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
        val modoCopiloto = sharedPreferences.getBoolean("modo_copiloto", false)

        // Permitir uso si no está conduciendo O si modo copiloto está activo
        return !_isDriving.value || modoCopiloto
    }


    fun checkCopilotMode() {
        val modoCopiloto = context?.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
            ?.getBoolean("modo_copiloto", false) ?: false

        if (modoCopiloto) {
            _isDriving.value = false
            drivingDetectionCount = 0
            stoppedDetectionCount = 0
        }
    }


    fun reset() {
        _isDriving.value = false
        lastLocation = null
        lastUpdateTime = 0L
        lastSpeed = 0f
        drivingDetectionCount = 0
        stoppedDetectionCount = 0
    }

    fun setDrivingState(isDriving: Boolean) {
        _isDriving.value = isDriving
    }
}