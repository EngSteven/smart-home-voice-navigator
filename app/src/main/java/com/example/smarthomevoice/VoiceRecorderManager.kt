package com.example.smarthomevoice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * Gestor de captura de audio de bajo nivel optimizado para el sistema de escucha continua (hands-free).
 *
 * Configura y administra el ciclo de vida del [AudioRecord] de Android, capturando flujos
 * de audio PCM en formato de punto flotante a 16kHz. Actúa como la capa de ingestión principal
 * y cuenta con un mecanismo de Detección de Actividad de Voz (VAD) basado en umbrales de energía
 * para descartar silencios y delegar únicamente audio válido al motor de procesamiento digital (DSP).
 */
class VoiceRecorderManager {

    // Parámetros acústicos requeridos estrictamente por el modelo ONNX
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_FLOAT

    // Tamaño del bloque correspondiente a exactamente 1 segundo de muestreo continuo
    private val bufferSize = 16000

    @Volatile
    private var isListening = false
    private var recordingJob: Job? = null

    /**
     * Inicia el bucle infinito de captura de audio en un hilo secundario manejado por Corrutinas.
     *
     * Implementa una puerta de ruido (Noise Gate) calculando la amplitud media rectificada
     * del bloque de audio. La fórmula utilizada es:
     * $$ A_{avg} = \frac{1}{N} \sum_{i=0}^{N-1} |x[i]| $$
     * donde $N$ es el `bufferSize` y $x[i]$ es el valor de la muestra de audio. Si $A_{avg}$
     * supera el umbral de calibración, el bloque se clona y se envía de forma asíncrona al consumidor.
     *
     * @param scope El [CoroutineScope] en el que se lanzará la tarea de grabación I/O.
     * @param onAudioReady Callback suspendido que se invoca cuando un bloque de audio válido
     * supera la puerta de ruido y está listo para la extracción de características (Espectrograma).
     */
    @SuppressLint("MissingPermission")
    fun startContinuousListening(
        scope: CoroutineScope,
        onAudioReady: suspend (FloatArray) -> Unit
    ) {
        if (isListening) return
        isListening = true

        recordingJob = scope.launch(Dispatchers.IO) {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                maxOf(minBufferSize, bufferSize * 2)
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                isListening = false
                return@launch
            }

            record.startRecording()
            val audioBuffer = FloatArray(bufferSize)

            try {
                while (isListening && isActive) {
                    var read = 0

                    // Llenado bloqueante del buffer para asegurar muestras completas (1 segundo)
                    while (read < bufferSize && isListening && isActive) {
                        val result = record.read(
                            audioBuffer,
                            read,
                            bufferSize - read,
                            AudioRecord.READ_BLOCKING
                        )
                        if (result < 0) break
                        read += result
                    }

                    // Cálculo de energía de la señal (Amplitud media rectificada)
                    var currentEnergy = 0f
                    for (i in 0 until bufferSize) {
                        currentEnergy += abs(audioBuffer[i])
                    }
                    val avgAmplitude = currentEnergy / bufferSize

                    // Aplicación del umbral de detección de voz (Noise Gate ~ 0.005f)
                    if (avgAmplitude > 0.005f) {
                        // Despacho defensivo: Se clona el arreglo para evitar condiciones de carrera
                        // mientras el micrófono sobreescribe el buffer original en la próxima iteración.
                        onAudioReady(audioBuffer.copyOf())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                record.stop()
                record.release()
            }
        }
    }

    /**
     * Interrumpe el bucle de grabación, cancela el Job de la corrutina y libera
     * los recursos de hardware asociados al micrófono.
     */
    fun stopListening() {
        isListening = false
        recordingJob?.cancel()
    }
}