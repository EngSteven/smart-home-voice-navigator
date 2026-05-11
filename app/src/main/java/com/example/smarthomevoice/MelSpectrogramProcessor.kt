package com.example.smarthomevoice

import kotlin.math.*

/**
 * Motor de Procesamiento Digital de Señales (DSP) para la extracción de características de audio.
 *
 * Transforma un flujo de muestras de audio crudas (dominio del tiempo) en un Espectrograma de Mel
 * (dominio de la frecuencia-tiempo), el cual sirve como tensor de entrada para la inferencia
 * del modelo acústico ONNX local.
 *
 * El pipeline de procesamiento implementa una Transformada Corta de Fourier (STFT), aplica un
 * banco de filtros triangulares en la escala Mel y convierte la energía resultante a una
 * escala logarítmica (decibelios).
 */
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
        // Precomputación de la ventana de Hann para mitigar la fuga espectral (spectral leakage)
        // en los extremos de cada frame durante la STFT.
        hannWindow = FloatArray(nFft) { i ->
            (0.5 * (1.0 - cos(2.0 * Math.PI * i / (nFft - 1)))).toFloat()
        }
        melFilterBank = createMelFilterBank()
    }

    /**
     * Procesa una señal de audio unidimensional y retorna un tensor aplanado.
     *
     * El algoritmo sigue estos pasos analíticos:
     * 1. Segmentación de la señal en tramos superpuestos multiplicados por una ventana de Hann.
     * 2. Aplicación de una FFT de tamaño `nFft` a cada segmento.
     * 3. Cálculo del espectro de potencia: $P[k] = \text{Re}(X[k])^2 + \text{Im}(X[k])^2$
     * 4. Mapeo del espectro lineal a la escala Mel utilizando el banco de filtros precomputado.
     * 5. Conversión de la energía a escala logarítmica (dB): $S_{dB} = 10 \cdot \log_{10}(S_{mel})$
     *
     * @param audioData Arreglo de muestras de audio en punto flotante a 16kHz.
     * @return Arreglo 1D de tamaño [128 * 128] que representa la matriz aplanada del espectrograma,
     *         lista para ser inyectada directamente en el modelo ONNX.
     */
    fun process(audioData: FloatArray): FloatArray {
        val nFrames = 1 + (audioData.size - nFft) / hopLength
        val spectrogram = Array(nMels) { FloatArray(size) { -100f } }

        val xReal = FloatArray(nFft)
        val xImag = FloatArray(nFft)

        for (i in 0 until minOf(nFrames, size)) {
            val start = i * hopLength

            for (j in 0 until nFft) {
                if (start + j < audioData.size) {
                    xReal[j] = audioData[start + j] * hannWindow[j]
                } else {
                    xReal[j] = 0f
                }
                xImag[j] = 0f
            }

            fft(xReal, xImag)

            val powerSpec = FloatArray(nFft / 2 + 1)
            for (j in powerSpec.indices) {
                powerSpec[j] = (xReal[j] * xReal[j] + xImag[j] * xImag[j])
            }

            for (m in 0 until nMels) {
                var melEnergy = 0f
                for (k in powerSpec.indices) {
                    melEnergy += powerSpec[k] * melFilterBank[m][k]
                }

                spectrogram[m][i] = if (melEnergy > 1e-10f) {
                    (10.0 * log10(melEnergy.toDouble())).toFloat()
                } else {
                    -100f
                }
            }
        }

        // Aplanado del tensor 2D a 1D contiguo en memoria para cumplir con los
        // requisitos topológicos del grafo del modelo ONNX (Dimensión esperada: 1x1x128x128)
        val flatSpectrogram = FloatArray(size * size)
        var index = 0
        for (m in 0 until size) {
            for (f in 0 until size) {
                flatSpectrogram[index++] = spectrogram[m][f]
            }
        }
        return flatSpectrogram
    }

    /**
     * Construye un banco de filtros triangulares superpuestos espaciados uniformemente
     * en la escala Mel, mapeados a los bins de frecuencia de la FFT.
     */
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

    /**
     * Convierte una frecuencia del dominio lineal (Hertz) a la escala psicoacústica Mel.
     * Fórmua utilizada: $m = 2595 \cdot \log_{10}\left(1 + \frac{f}{700}\right)$
     */
    private fun hzToMel(hz: Double): Float = (2595.0 * log10(1.0 + hz / 700.0)).toFloat()

    /**
     * Convierte un valor de la escala psicoacústica Mel al dominio de frecuencia lineal (Hertz).
     * Fórmula utilizada: $f = 700 \cdot \left(10^{\frac{m}{2595}} - 1\right)$
     */
    private fun melToHz(mel: Float): Double = 700.0 * (10.0.pow(mel / 2595.0) - 1.0)

    /**
     * Implementación in-place del algoritmo Radix-2 Cooley-Tukey para la Transformada
     * Rápida de Fourier (FFT).
     *
     * @param xReal Parte real de la señal enventanada (se sobrescribe con la parte real del espectro).
     * @param xImag Parte imaginaria (inicializada en 0, se sobrescribe con la parte imaginaria del espectro).
     */
    private fun fft(xReal: FloatArray, xImag: FloatArray) {
        val n = xReal.size
        var j = 0

        // Ordenamiento por reversión de bits (Bit-reversal permutation)
        for (i in 0 until n - 1) {
            if (i < j) {
                val tempReal = xReal[i]; xReal[i] = xReal[j]; xReal[j] = tempReal
                val tempImag = xImag[i]; xImag[i] = xImag[j]; xImag[j] = tempImag
            }
            var m = n / 2
            while (m <= j) { j -= m; m /= 2 }
            j += m
        }

        // Cálculo de las mariposas de Danielson-Lanczos (Danielson-Lanczos butterfly)
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