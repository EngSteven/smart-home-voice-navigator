package com.example.smarthomevoice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: SmartHomeViewModel by viewModels()

    // Instanciamos tus clases de procesamiento local
    private lateinit var onnxManager: OnnxModelManager
    private val melProcessor = MelSpectrogramProcessor()
    private val voiceRecorder = VoiceRecorderManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitamos permisos al inicio si no los tenemos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        onnxManager = OnnxModelManager(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAppUI(
                        viewModel = viewModel,
                        // Enviamos una función vacía porque el botón ya no hace nada, es pura decoración
                        onRecordTriggered = { /* Ya no hace nada, la escucha es automática */ }
                    )
                }
            }
        }
    }

    // Encendemos la escucha infinita CADA VEZ que la app esté visible en pantalla
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startAlwaysOnAssistant()
        }
    }

    // Apagamos el micrófono si el usuario minimiza la app para que Android no lance una excepción
    override fun onPause() {
        super.onPause()
        voiceRecorder.stopListening()
        viewModel.setListening(false)
    }

    // Función que mantiene vivo el bucle
    private fun startAlwaysOnAssistant() {
        // Mantenemos la UI con la animación de que siempre está escuchando
        viewModel.setListening(true)

        voiceRecorder.startContinuousListening(lifecycleScope) { audioData ->
            // Este bloque se ejecuta SOLAMENTE cuando el micrófono detecta un sonido fuerte

            // 1. Procesamiento acústico (128x128)
            val flatSpectrogram = melProcessor.process(audioData)

            // 2. Predicción de IA
            val prediction = onnxManager.predict(flatSpectrogram)

            // 3. Reacción en la Interfaz
            // No muestre nada en caso de no reconocer el comando.
            //if (prediction != "unknown") {
            //    // Volvemos al hilo principal para actualizar la UI según el comando
            //    lifecycleScope.launch(Dispatchers.Main) {
            //        viewModel.processVoiceCommand(prediction)
            //    }
            //}
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.processVoiceCommand(prediction)
            }
        }
    }
}

@Composable
fun MainAppUI(viewModel: SmartHomeViewModel, onRecordTriggered: () -> Unit) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val focusedIndex by viewModel.focusedIndex.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val lastCommand by viewModel.lastCommand.collectAsState()
    val isDeviceOn by viewModel.isDeviceOn.collectAsState()

    val focusedRoutineIndex by viewModel.focusedRoutineIndex.collectAsState()
    val isRoutineRunning by viewModel.isRoutineRunning.collectAsState()

    when (currentScreen) {
        AppScreen.DASHBOARD -> {
            DashboardScreen(
                focusedIndex = focusedIndex,
                isListening = isListening,
                lastCommand = lastCommand,
                onRecordClick = onRecordTriggered
            )
        }
        AppScreen.DEVICE -> {
            val deviceName = when (focusedIndex) {
                0 -> "Luces"
                1 -> "Ventilador"
                2 -> "Televisión"
                else -> "Dispositivo"
            }

            DeviceScreen(
                deviceName = deviceName,
                isDeviceOn = isDeviceOn,
                isListening = isListening,
                lastCommand = lastCommand,
                onRecordClick = onRecordTriggered
            )
        }
        AppScreen.ROUTINE -> {
            RoutineScreen(
                focusedRoutineIndex = focusedRoutineIndex,
                isRoutineRunning = isRoutineRunning,
                isListening = isListening,
                lastCommand = lastCommand,
                onRecordClick = onRecordTriggered
            )
        }
        AppScreen.CONFIRMATION -> {
            val (name, desc) = when (focusedRoutineIndex) {
                0 -> "¿Activar Modo Estudio?" to "Se encenderán las luces de trabajo y se silenciarán notificaciones."
                1 -> "¿Activar Modo Noche?" to "Se apagarán todas las luces y dispositivos para dormir."
                2 -> "¿Activar Bienvenida?" to "Se encenderán las luces principales y el centro de medios."
                else -> "¿Confirmar Acción?" to "Presiona o di 'yes' para proceder con la rutina seleccionada."
            }
            ConfirmationScreen(
                actionName = name,
                actionDescription = desc,
                isListening = isListening,
                lastCommand = lastCommand,
                onRecordClick = onRecordTriggered
            )
        }
    }
}