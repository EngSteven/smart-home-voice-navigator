package com.example.smarthomevoice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: SmartHomeViewModel by viewModels()

    // Instanciamos tus clases de procesamiento local
    private lateinit var onnxManager: OnnxModelManager
    private val melProcessor = MelSpectrogramProcessor()
    private val voiceRecorder = VoiceRecorderManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        // Inicializar el modelo (asumiendo que tu OnnxModelManager requiere el contexto)
        onnxManager = OnnxModelManager(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAppUI(
                        viewModel = viewModel,
                        onRecordTriggered = { startRecordingAndInference() }
                    )
                }
            }
        }
    }

    // Función que maneja el hilo de I/O para el audio y la inferencia
    private fun startRecordingAndInference() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Avisamos a la UI que empezamos a grabar
            withContext(Dispatchers.Main) { viewModel.setListening(true) }

            // 1. Grabar audio usando tu método original (puede ser null)
            val audioData: FloatArray? = voiceRecorder.recordSmartOneSecond()

            if (audioData != null) {
                // 2. Preprocesar a Mel Espectrograma
                val flatSpectrogram = melProcessor.process(audioData)

                // 3. Inferencia con ONNX
                val prediction = onnxManager.predict(flatSpectrogram)

                // 4. Volver al hilo principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    viewModel.processVoiceCommand(prediction)
                    viewModel.setListening(false)
                }
            } else {
                // Si el audio es null (volumen muy bajo), restauramos la UI
                withContext(Dispatchers.Main) {
                    viewModel.processVoiceCommand("silencio") // Opcional: manejar este estado en la UI
                    viewModel.setListening(false)
                }
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

    // estados para Rutinas
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

            // Llamamos a nuestra nueva pantalla
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