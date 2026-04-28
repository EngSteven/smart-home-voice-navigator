package com.example.smarthomevoice

import kotlin.math.*

class MelSpectrogramProcessor {

    private val sampleRate = 16000
    private val nFft = 1024
    private val hopLength = 125
    private val nMels = 128
    private val fMin = 0.0
    private val fMax = sampleRate / 2.0

    private val melFilterBank: Array<FloatArray>
    private val hannWindow: FloatArray
    private val size = 128

    init {
        hannWindow = FloatArray(nFft) { i ->
            (0.5 * (1.0 - cos(2.0 * Math.PI * i / (nFft - 1)))).toFloat()
        }
        melFilterBank = createMelFilterBank()
    }

    fun process(audioData: FloatArray): FloatArray {
        // Calculamos cuántos frames (columnas) saldrán
        val nFrames = 1 + (audioData.size - nFft) / hopLength
        val spectrogram = Array(nMels) { FloatArray(size) { -100f } }

        val xReal = FloatArray(nFft)
        val xImag = FloatArray(nFft)

        for (i in 0 until minOf(nFrames, size)) {
            val start = i * hopLength

            // 1. Aplicar ventana de Hann
            for (j in 0 until nFft) {
                if (start + j < audioData.size) {
                    xReal[j] = audioData[start + j] * hannWindow[j]
                } else {
                    xReal[j] = 0f
                }
                xImag[j] = 0f
            }

            // 2. Calcular FFT
            fft(xReal, xImag)

            // 3. Espectro de poder
            val powerSpec = FloatArray(nFft / 2 + 1)
            for (j in powerSpec.indices) {
                powerSpec[j] = (xReal[j] * xReal[j] + xImag[j] * xImag[j])
            }

            // 4. Aplicar Filtros Mel y convertir a decibelios (Log10)
            for (m in 0 until nMels) {
                var melEnergy = 0f
                for (k in powerSpec.indices) {
                    melEnergy += powerSpec[k] * melFilterBank[m][k]
                }

                // Conversión a dB
                spectrogram[m][i] = if (melEnergy > 1e-10f) {
                    (10.0 * log10(melEnergy.toDouble())).toFloat()
                } else {
                    -100f
                }
            }
        }

        // 5. Aplanar la matriz a un arreglo 1D para ONNX
        val flatSpectrogram = FloatArray(size * size)
        var index = 0
        for (m in 0 until size) {
            for (f in 0 until size) {
                flatSpectrogram[index++] = spectrogram[m][f]
            }
        }
        return flatSpectrogram
    }

    private fun createMelFilterBank(): Array<FloatArray> {
        val minMel = hzToMel(fMin)
        val maxMel = hzToMel(fMax)
        val melPoints = FloatArray(nMels + 2) { i -> minMel + i * (maxMel - minMel) / (nMels + 1) }
        val hzPoints = melPoints.map { melToHz(it).toFloat() }.toFloatArray()
        val binPoints = hzPoints.map { floor((nFft + 1) * it / sampleRate).toInt() }.toIntArray()

        val filterBank = Array(nMels) { FloatArray(nFft / 2 + 1) }
        for (m in 1..nMels) {
            val start = binPoints[m - 1]
            val center = binPoints[m]
            val end = binPoints[m + 1]

            for (k in start until center) {
                filterBank[m - 1][k] = (k - start).toFloat() / (center - start).toFloat()
            }
            for (k in center until end) {
                filterBank[m - 1][k] = (end - k).toFloat() / (end - center).toFloat()
            }
        }
        return filterBank
    }

    private fun hzToMel(hz: Double): Float = (2595.0 * log10(1.0 + hz / 700.0)).toFloat()
    private fun melToHz(mel: Float): Double = 700.0 * (10.0.pow(mel / 2595.0) - 1.0)

    private fun fft(xReal: FloatArray, xImag: FloatArray) {
        val n = xReal.size
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                val tempReal = xReal[i]; xReal[i] = xReal[j]; xReal[j] = tempReal
                val tempImag = xImag[i]; xImag[i] = xImag[j]; xImag[j] = tempImag
            }
            var m = n / 2
            while (m <= j) { j -= m; m /= 2 }
            j += m
        }
        var size = 2
        while (size <= n) {
            val halfSize = size / 2
            val theta = -2.0 * PI / size
            val wpReal = cos(theta).toFloat()
            val wpImag = sin(theta).toFloat()
            for (i in 0 until n step size) {
                var wReal = 1.0f; var wImag = 0.0f
                for (k in 0 until halfSize) {
                    val evenReal = xReal[i + k]
                    val evenImag = xImag[i + k]
                    val oddReal = xReal[i + k + halfSize]
                    val oddImag = xImag[i + k + halfSize]
                    val tReal = wReal * oddReal - wImag * oddImag
                    val tImag = wReal * oddImag + wImag * oddReal
                    xReal[i + k + halfSize] = evenReal - tReal
                    xImag[i + k + halfSize] = evenImag - tImag
                    xReal[i + k] = evenReal + tReal
                    xImag[i + k] = evenImag + tImag
                    val nextWReal = wReal * wpReal - wImag * wpImag
                    val nextWImag = wReal * wpImag + wImag * wpReal
                    wReal = nextWReal; wImag = nextWImag
                }
            }
            size *= 2
        }
    }
}