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

/**
 * Actividad principal de la aplicación.
 *
 * Gestiona el ciclo de vida de la interfaz de usuario, la solicitud de permisos
 * de hardware (micrófono) y orquesta el pipeline de reconocimiento de voz continuo.
 * Conecta la captura de audio crudo con el procesamiento de señales (DSP), el
 * motor de inferencia local (ONNX) y la actualización del estado global (ViewModel).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: SmartHomeViewModel by viewModels()

    private lateinit var onnxManager: OnnxModelManager
    private val melProcessor = MelSpectrogramProcessor()
    private val voiceRecorder = VoiceRecorderManager()

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Validación y solicitud de permisos de grabación en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }

        onnxManager = OnnxModelManager(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppUI(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * Reanuda el servicio de escucha continua (hands-free) cuando la aplicación
     * entra en primer plano, asumiendo que los permisos han sido concedidos.
     */
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startAlwaysOnAssistant()
        }
    }

    /**
     * Interrumpe la captura de audio y libera los recursos del micrófono
     * cuando la aplicación pasa a segundo plano para prevenir excepciones del sistema operativo.
     */
    override fun onPause() {
        super.onPause()
        voiceRecorder.stopListening()
        viewModel.setListening(false)
    }

    /**
     * Inicializa el bucle infinito de asistencia por voz.
     *
     * Establece el flujo de datos: Captura (VoiceRecorder) -> Extracción de características (MelSpectrogram)
     * -> Inferencia (ONNX) -> Mutación de estado (ViewModel). Desvía las tareas intensivas a hilos
     * secundarios y retorna al hilo principal exclusivamente para la actualización reactiva de la UI.
     */
    private fun startAlwaysOnAssistant() {
        viewModel.setListening(true)

        voiceRecorder.startContinuousListening(lifecycleScope) { audioData ->
            // 1. Procesamiento acústico de la señal a tensor 1D (128x128)
            val flatSpectrogram = melProcessor.process(audioData)

            // 2. Inferencia de red neuronal local
            val prediction = onnxManager.predict(flatSpectrogram)

            // 3. Filtrado y enrutamiento del comando hacia la máquina de estados.
            // Se ignoran predicciones de baja confianza para evitar perturbaciones en la UI.
            if (prediction != "unknown") {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.processVoiceCommand(prediction)
                }
            }
        }
    }
}

/**
 * Componente raíz de la interfaz gráfica que enruta la navegación basándose
 * en el estado reactivo del `ViewModel`.
 *
 * @param viewModel Instancia principal de la máquina de estados de la aplicación.
 */
@Composable
fun MainAppUI(viewModel: SmartHomeViewModel) {
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
                lastCommand = lastCommand
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
                lastCommand = lastCommand
            )
        }
        AppScreen.ROUTINE -> {
            RoutineScreen(
                focusedRoutineIndex = focusedRoutineIndex,
                isRoutineRunning = isRoutineRunning,
                isListening = isListening,
                lastCommand = lastCommand
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
                lastCommand = lastCommand
            )
        }
    }
}