package com.example.smarthomevoice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class VoiceRecorderManager {
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_FLOAT

    private val windowSize = 16000 // El 1 segundo exacto que necesita ONNX
    private val totalRecordSize = 32000 // Grabamos 2 segundos para dar margen de tiempo

    private val maxAvgAmplitude = 0f    // Filtro para no captar ruido de fondo basura

    @SuppressLint("MissingPermission")
    suspend fun recordSmartOneSecond(): FloatArray? = withContext(Dispatchers.IO) {
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                maxOf(minBufferSize, totalRecordSize * 4)
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) return@withContext null

            // 1. Grabamos 2 segundos completos
            val fullAudioData = FloatArray(totalRecordSize)
            record.startRecording()

            var read = 0
            while (read < totalRecordSize) {
                val result = record.read(fullAudioData, read, totalRecordSize - read, AudioRecord.READ_BLOCKING)
                if (result < 0) break
                read += result
            }

            record.stop()
            record.release()

            // 2. ALGORITMO DE AUTO-ENFOQUE (Buscamos la voz)
            var maxEnergy = 0f
            var bestStartIdx = 0

            // Escaneamos saltando de a 500 muestras para buscar la ventana de 1 segundo más ruidosa
            for (i in 0..totalRecordSize - windowSize step 500) {
                var currentEnergy = 0f
                for (j in i until i + windowSize) {
                    currentEnergy += abs(fullAudioData[j])
                }
                if (currentEnergy > maxEnergy) {
                    maxEnergy = currentEnergy
                    bestStartIdx = i
                }
            }

            // 3. FILTRO DE RUIDO DE FONDO / SILENCIO
            // Si la energía promedio de ese pedazo "ruidoso" es muy baja, es solo aire o silencio
            //val avgAmplitude = maxEnergy / windowSize
            //if (avgAmplitude < maxAvgAmplitude) {
            //    return@withContext null
            //}

            // 4. Recortamos el audio perfecto y se lo damos a ONNX
            return@withContext fullAudioData.copyOfRange(bestStartIdx, bestStartIdx + windowSize)

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}