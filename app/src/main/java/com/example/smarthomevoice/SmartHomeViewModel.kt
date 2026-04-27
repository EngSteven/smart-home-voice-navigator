package com.example.smarthomevoice

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Definimos las pantallas posibles de nuestra app
enum class AppScreen {
    DASHBOARD, DEVICE, ROUTINE, CONFIRMATION
}

class SmartHomeViewModel : ViewModel() {

    // Estado actual de la pantalla
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Estado del foco en el Dashboard (Índices del 0 al 3 para una grilla 2x2)
    // 0: Luces (Arriba-Izq), 1: Ventilador (Arriba-Der)
    // 2: TV (Abajo-Izq), 3: Rutinas (Abajo-Der)
    private val _focusedIndex = MutableStateFlow(0)
    val focusedIndex: StateFlow<Int> = _focusedIndex.asStateFlow()

    // Estado para saber si estamos grabando audio
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Estado para mostrar en pantalla qué entendió el modelo
    private val _lastCommand = MutableStateFlow("Ninguno")
    val lastCommand: StateFlow<String> = _lastCommand.asStateFlow()

    // Nuevo estado: Define si el dispositivo seleccionado está encendido (true) o apagado (false)
    private val _isDeviceOn = MutableStateFlow(false)
    val isDeviceOn: StateFlow<Boolean> = _isDeviceOn.asStateFlow()

    private val _focusedRoutineIndex = MutableStateFlow(0)
    val focusedRoutineIndex: StateFlow<Int> = _focusedRoutineIndex.asStateFlow()

    // Indica si la rutina actual está activa ("go") o detenida ("stop")
    private val _isRoutineRunning = MutableStateFlow(false)
    val isRoutineRunning: StateFlow<Boolean> = _isRoutineRunning.asStateFlow()

    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    // Función principal que recibirá el String del modelo ONNX
    fun processVoiceCommand(command: String) {
        _lastCommand.value = command // Guardamos el comando para mostrarlo en UI
        val current = _focusedIndex.value

        when (_currentScreen.value) {
            AppScreen.DASHBOARD -> {
                when (command) {
                    "right" -> if (current == 0 || current == 2) _focusedIndex.value += 1
                    "left" -> if (current == 1 || current == 3) _focusedIndex.value -= 1
                    "down" -> if (current == 0 || current == 1) _focusedIndex.value += 2
                    "up" -> if (current == 2 || current == 3) _focusedIndex.value -= 2
                    "yes" -> handleSelection(current)
                }
            }
            AppScreen.DEVICE -> {
                // Nueva lógica exclusiva para la pantalla de Dispositivos
                when (command) {
                    "on" -> _isDeviceOn.value = true
                    "off" -> _isDeviceOn.value = false
                    "no" -> {
                        _currentScreen.value = AppScreen.DASHBOARD
                        _isDeviceOn.value = false // Reseteamos el estado al salir
                    }
                }
            }

            AppScreen.ROUTINE -> {
                val currentRoutine = _focusedRoutineIndex.value
                when (command) {
                    "down" -> if (currentRoutine < 2) {
                        _focusedRoutineIndex.value += 1
                        _isRoutineRunning.value = false
                    }
                    "up" -> if (currentRoutine > 0) {
                        _focusedRoutineIndex.value -= 1
                        _isRoutineRunning.value = false
                    }
                    "go" -> {
                        _currentScreen.value = AppScreen.CONFIRMATION
                    }
                    "stop" -> _isRoutineRunning.value = false
                    "no" -> {
                        _currentScreen.value = AppScreen.DASHBOARD
                        _focusedRoutineIndex.value = 0
                        _isRoutineRunning.value = false
                    }
                }
            }
            AppScreen.CONFIRMATION -> {
                when (command) {
                    "yes" -> {
                        // Acción confirmada: Activamos la rutina y regresamos a la pantalla anterior
                        _isRoutineRunning.value = true
                        _currentScreen.value = AppScreen.ROUTINE
                    }
                    "no" -> {
                        // Acción cancelada: Regresamos sin activar nada
                        _currentScreen.value = AppScreen.ROUTINE
                    }
                }
            }
        }
    }

    private fun handleSelection(selectedIndex: Int) {
        when (selectedIndex) {
            0, 1, 2 -> _currentScreen.value = AppScreen.DEVICE // Luces, Ventilador, TV
            3 -> _currentScreen.value = AppScreen.ROUTINE // Rutinas
        }
    }
}