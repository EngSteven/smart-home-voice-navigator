package com.example.smarthomevoice

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.nio.FloatBuffer
import kotlin.math.exp

/**
 * Gestor del ciclo de vida y ejecución del motor de inferencia local (ONNX Runtime).
 *
 * Se encarga de instanciar el entorno de ejecución, transferir los artefactos del modelo
 * desde el empaquetado del APK hacia el almacenamiento accesible en caché, y despachar
 * los tensores de audio (espectrogramas) para obtener predicciones de comandos de voz.
 *
 * @param context Contexto de la aplicación utilizado para acceder a los `assets` y al sistema de archivos local.
 */
class OnnxModelManager(context: Context) {

    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null

    /**
     * Vocabulario de red neuronal. El orden de los índices (0-9) corresponde
     * estrictamente a la topología de salida (logits) definida durante el entrenamiento.
     */
    private val classes = arrayOf(
        "yes", "no", "up", "down", "left", "right", "on", "off", "stop", "go"
    )

    companion object {
        private const val TAG = "OnnxModelManager"
        private const val CONFIDENCE_THRESHOLD = 0.75f
    }

    init {
        try {
            val modelName = "smart_home_voice_navigator.onnx"
            val dataName = "smart_home_voice_navigator.onnx.data"

            val modelFile = File(context.cacheDir, modelName)
            val dataFile = File(context.cacheDir, dataName)

            // Extracción segura del grafo computacional (.onnx)
            if (!modelFile.exists()) {
                context.assets.open(modelName).use { input ->
                    modelFile.outputStream().use { output -> input.copyTo(output) }
                }
            }

            // Extracción segura de los tensores de pesos externos (.data).
            // Requerido por ONNX Runtime para modelos que exceden el límite de tamaño
            // de protobuf (2GB) o están particionados.
            if (!dataFile.exists()) {
                context.assets.open(dataName).use { input ->
                    dataFile.outputStream().use { output -> input.copyTo(output) }
                }
            }

            // Inicialización de la sesión utilizando la ruta absoluta para permitir
            // la resolución dinámica de dependencias (.data) adyacentes al modelo base.
            ortSession = ortEnvironment.createSession(modelFile.absolutePath)
            Log.i(TAG, "Grafo computacional y pesos ONNX cargados exitosamente.")

        } catch (e: Exception) {
            Log.e(TAG, "Fallo crítico en la inicialización del motor ONNX", e)
        }
    }

    /**
     * Ejecuta una pasada frontal (forward pass) en la red neuronal utilizando el
     * espectrograma proporcionado.
     *
     * Implementa internamente la función Softmax con estabilización numérica para convertir
     * los logits crudos $\mathbf{z}$ en una distribución de probabilidad $P$:
     * $$ P(y = j \mid \mathbf{z}) = \frac{e^{z_j - \max(\mathbf{z})}}{\sum_{k} e^{z_k - \max(\mathbf{z})}} $$
     *
     * Las predicciones que no superan el umbral de confianza ([CONFIDENCE_THRESHOLD])
     * son mitigadas preventivamente para evitar "falsos positivos" en el sistema de escucha continua.
     *
     * @param flatSpectrogram Tensor aplanado 1D correspondiente a la extracción de características (Dim: 128x128).
     * @return El identificador en formato texto de la clase inferida, o "unknown" si la confianza es baja.
     */
    fun predict(flatSpectrogram: FloatArray): String {
        val session = ortSession ?: run {
            Log.e(TAG, "Sesión no inicializada al invocar predict()")
            return "unknown"
        }

        // Definición de la topología del tensor de entrada: [Batch, Channels, Height, Width]
        val shape = longArrayOf(1, 1, 128, 128)
        val floatBuffer = FloatBuffer.wrap(flatSpectrogram)
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape)

        val inputName = session.inputNames.iterator().next()
        val results = session.run(mapOf(inputName to inputTensor))

        val output = results[0].value as Array<FloatArray>
        val logits = output[0]

        // Cálculo de Softmax con estabilización numérica (resta del valor máximo)
        val maxLogit = logits.maxOrNull() ?: 0f
        var sumExp = 0f
        val probabilities = FloatArray(logits.size)

        for (i in logits.indices) {
            probabilities[i] = exp(logits[i] - maxLogit)
            sumExp += probabilities[i]
        }
        for (i in probabilities.indices) {
            probabilities[i] /= sumExp
        }

        // Identificación de la clase dominante (ArgMax)
        var maxIdx = 0
        var maxProb = probabilities[0]
        for (i in 1 until probabilities.size) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIdx = i
            }
        }

        // Liberación proactiva de la memoria nativa JNI
        inputTensor.close()
        results.close()

        // Filtrado por umbral de confianza
        if (maxProb < CONFIDENCE_THRESHOLD) {
            Log.d(TAG, "Predicción descartada. Confianza: ${(maxProb * 100).toInt()}% para '${classes[maxIdx]}'")
            return "unknown"
        }

        Log.d(TAG, "Inferencia confirmada: '${classes[maxIdx]}' al ${(maxProb * 100).toInt()}%")
        return classes[maxIdx]
    }
}