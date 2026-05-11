package com.example.smarthomevoice

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enumeración que define los destinos de navegación posibles dentro de la aplicación.
 */
enum class AppScreen {
    DASHBOARD, DEVICE, ROUTINE, CONFIRMATION
}

/**
 * ViewModel principal que gestiona el estado de la interfaz de usuario y procesa
 * la lógica de navegación basada en voz.
 *
 * Actúa como la máquina de estados que recibe las inferencias del modelo ONNX local
 * (mediante el sistema de escucha continua) y muta el estado de la UI (navegación espacial,
 * selección de dispositivos y ejecución de rutinas) de forma reactiva.
 */
class SmartHomeViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    /** Estado actual de la navegación en la aplicación. */
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _focusedIndex = MutableStateFlow(0)
    /**
     * Puntero de navegación espacial para la cuadrícula 2x2 del panel principal.
     * Mapeo de índices: 0 (Sup-Izq), 1 (Sup-Der), 2 (Inf-Izq), 3 (Inf-Der).
     */
    val focusedIndex: StateFlow<Int> = _focusedIndex.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    /** Bandera que indica si el servicio de reconocimiento de voz continuo está activo. */
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastCommand = MutableStateFlow("Ninguno")
    /** Último comando validado emitido por el motor de inferencia. */
    val lastCommand: StateFlow<String> = _lastCommand.asStateFlow()

    private val _isDeviceOn = MutableStateFlow(false)
    /** Estado operativo del dispositivo actualmente en contexto (`true` = ON, `false` = OFF). */
    val isDeviceOn: StateFlow<Boolean> = _isDeviceOn.asStateFlow()

    private val _focusedRoutineIndex = MutableStateFlow(0)
    /** Índice que representa la rutina actualmente seleccionada en la lista vertical. */
    val focusedRoutineIndex: StateFlow<Int> = _focusedRoutineIndex.asStateFlow()

    private val _isRoutineRunning = MutableStateFlow(false)
    /** Bandera que indica si la rutina seleccionada se encuentra en ejecución. */
    val isRoutineRunning: StateFlow<Boolean> = _isRoutineRunning.asStateFlow()

    /**
     * Actualiza el estado global del sistema de micrófono.
     *
     * @param listening `true` si el sistema comienza a escuchar, `false` en caso contrario.
     */
    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    /**
     * Evalúa y procesa una etiqueta de comando proveniente del motor de inferencia ONNX.
     *
     * El comportamiento del comando es contextual y depende de la pantalla
     * en la que se encuentre el usuario actualmente (`_currentScreen`).
     *
     * @param command Cadena de texto correspondiente a la acción inferida (ej. "up", "down", "yes", "no").
     */
    fun processVoiceCommand(command: String) {
        _lastCommand.value = command
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
                when (command) {
                    "on" -> _isDeviceOn.value = true
                    "off" -> _isDeviceOn.value = false
                    "no" -> {
                        _currentScreen.value = AppScreen.DASHBOARD
                        _isDeviceOn.value = false
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
                        // Las rutinas requieren validación explícita antes de ejecutarse
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
                        _isRoutineRunning.value = true
                        _currentScreen.value = AppScreen.ROUTINE
                    }
                    "no" -> {
                        _currentScreen.value = AppScreen.ROUTINE
                    }
                }
            }
        }
    }

    /**
     * Enruta la navegación desde el panel principal hacia la pantalla de detalle correspondiente.
     *
     * @param selectedIndex Índice del componente seleccionado en la cuadrícula del panel principal.
     */
    private fun handleSelection(selectedIndex: Int) {
        when (selectedIndex) {
            0, 1, 2 -> _currentScreen.value = AppScreen.DEVICE
            3 -> _currentScreen.value = AppScreen.ROUTINE
        }
    }
}