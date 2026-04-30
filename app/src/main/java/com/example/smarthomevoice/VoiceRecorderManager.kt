package com.example.smarthomevoice // Asegúrate de que sea tu paquete

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.abs

class VoiceRecorderManager {
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_FLOAT
    private val bufferSize = 16000 // 1 segundo exacto de audio

    @Volatile
    private var isListening = false
    private var recordingJob: Job? = null

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
                // Bucle infinito mientras el micrófono deba estar encendido
                while (isListening && isActive) {
                    var read = 0
                    // Llenamos el buffer de 1 segundo (16,000 muestras)
                    while (read < bufferSize && isListening && isActive) {
                        val result = record.read(audioBuffer, read, bufferSize - read, AudioRecord.READ_BLOCKING)
                        if (result < 0) break
                        read += result
                    }

                    // 1. Calculamos la energía del audio capturado
                    var currentEnergy = 0f
                    for (i in 0 until bufferSize) {
                        currentEnergy += abs(audioBuffer[i])
                    }
                    val avgAmplitude = currentEnergy / bufferSize

                    // 2. Puerta de Ruido (Noise Gate): Si hay un sonido fuerte (voz), lo enviamos a procesar.
                    // Si es puro silencio, el ciclo simplemente lo descarta y vuelve a grabar,
                    // ahorrando miles de cálculos a la IA.
                    if (avgAmplitude > 0.005f) { // Tu umbral calibrado
                        // Pasamos una COPIA del buffer para que el micrófono pueda
                        // seguir grabando el siguiente segundo sin sobreescribir este
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

    fun stopListening() {
        isListening = false
        recordingJob?.cancel()
    }
}