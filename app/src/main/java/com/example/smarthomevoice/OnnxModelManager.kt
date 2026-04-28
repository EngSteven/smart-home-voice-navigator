package com.example.smarthomevoice


import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

class OnnxModelManager(context: Context) {

    // Entorno y sesión de ONNX Runtime
    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null

    //  El orden estricto de las 10 clases tal cual se definieron en Python
    private val classes = arrayOf("yes", "no", "up", "down", "left", "right", "on", "off", "stop", "go")

    init {
        try {
            val modelName = "smart_home_voice_navigator.onnx"
            val dataName = "smart_home_voice_navigator.onnx.data"

            // 1. Definimos las rutas en la memoria caché real del celular
            val modelFile = java.io.File(context.cacheDir, modelName)
            val dataFile = java.io.File(context.cacheDir, dataName)

            // 2. Extraemos el .onnx de la carpeta comprimida assets
            if (!modelFile.exists()) {
                context.assets.open(modelName).use { input ->
                    modelFile.outputStream().use { output -> input.copyTo(output) }
                }
            }

            // 3. Extraemos los pesos (.data) de la carpeta comprimida assets
            if (!dataFile.exists()) {
                context.assets.open(dataName).use { input ->
                    dataFile.outputStream().use { output -> input.copyTo(output) }
                }
            }

            // 4. Inicializamos la sesión pasándole la RUTA REAL del archivo (no los bytes)
            // Al darle la ruta, ONNX automáticamente encuentra el .data que está a la par
            ortSession = ortEnvironment.createSession(modelFile.absolutePath)
            println("ONNX: Modelo y pesos cargados exitosamente desde caché.")

        } catch (e: Exception) {
            e.printStackTrace()
            println("ONNX: Error al cargar el modelo o los pesos.")
        }
    }

    /**
     * Recibe un arreglo plano de flotantes que representa el Espectrograma Mel
     * y devuelve el comando predicho (String).
     */
    fun predict(flatSpectrogram: FloatArray): String {
        val session = ortSession ?: return "Error: Sesión no inicializada"

        val shape = longArrayOf(1, 1, 128, 128)
        val floatBuffer = java.nio.FloatBuffer.wrap(flatSpectrogram)
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape)

        val inputName = session.inputNames.iterator().next()
        val results = session.run(mapOf(inputName to inputTensor))

        // Extraemos los "logits" (números crudos)
        val output = results[0].value as Array<FloatArray>
        val logits = output[0]

        // --- INICIO DE SOFTMAX (Convertir a porcentajes de 0 a 1) ---
        val maxLogit = logits.maxOrNull() ?: 0f
        var sumExp = 0f
        val probabilities = FloatArray(logits.size)

        for (i in logits.indices) {
            probabilities[i] = kotlin.math.exp(logits[i] - maxLogit)
            sumExp += probabilities[i]
        }
        for (i in probabilities.indices) {
            probabilities[i] /= sumExp
        }
        // --- FIN DE SOFTMAX ---

        // Buscamos el ganador y su porcentaje de seguridad
        var maxIdx = 0
        var maxProb = probabilities[0]
        for (i in 1 until probabilities.size) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIdx = i
            }
        }

        inputTensor.close()
        results.close()


        // Si el modelo está menos del 75% seguro (0.75f), lo descartamos
        if (maxProb < 0.75f) {
            println("ONNX: Comando descartado por baja confianza: ${(maxProb * 100).toInt()}% para ${classes[maxIdx]}")
            return "unknown"
        }

        println("ONNX: Predicción segura: ${classes[maxIdx]} al ${(maxProb * 100).toInt()}%")
        return classes[maxIdx]
    }
}