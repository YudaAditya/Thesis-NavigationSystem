import android.content.Context
import android.view.accessibility.AccessibilityManager
import org.vosk.android.Recognizer
import org.vosk.android.SpeechService
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs

fun main() {
    val context: Context = TODO() // Pastikan Anda telah mengatur Context sesuai dengan lingkungan Anda
    val textToSpeechHelper = TextToSpeechHelper(context)
    textToSpeechHelper.initializeTTS()

    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val modelPath = TODO() // Path ke model Vosk yang telah diunduh
    val voskModel = VoskModel(modelPath)
    voskModel.loadModel()
    val voskRecognizer = VoskRecognizer(voskModel.getModel(), 16000.0f)
    voskRecognizer.startRecognizer()

    val finalDestination = getFinalDestinationFromVoiceInput(voskRecognizer.getRecognizer())
    println("Tujuan akhir pengguna adalah: $finalDestination")

    val initialPosition = doubleArrayOf(0.0, 0.0)
    val initialCovariance = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))
    val rssiDao: RssiDao = RoomRssiDao()
    var currentFloor = 1
    val finalPosition = Position(3.0, 1.0)
    val startNodeId = FloorGraph.floorGraph[currentFloor]?.firstOrNull { it.position == Position(initialPosition[0], initialPosition[1]) }?.id ?: 1
    val targetNodeId = FloorGraph.floorGraph[2]?.firstOrNull { it.position == finalPosition }?.id ?: 6
    val shortestPath = FloorGraph.findShortestPath(startNodeId, targetNodeId)
    val kalmanFilter = KalmanFilter(initialPosition, initialCovariance)

    println("Jalur terpendek dari lantai 1 ke lantai 2:")
    for (node in shortestPath) {
        println("Node ${node.id} (${node.position.x}, ${node.position.y})")
    }

    for (floorNode in shortestPath) {
        val floorNodeId = floorNode.id
        val rssiMeasurements = rssiDao.getRssiMeasurementsForFloor(floorNodeId)

        for (rssiMeasurement in rssiMeasurements) {
            val kalmanPrediction = kalmanFilter.predict()
            val rssiValues = rssiMeasurement.rssiValues.toDoubleArray()
            kalmanFilter.update(rssiValues)

            // Cetak hasil prediksi dan pengukuran
            println("Hasil Prediksi: ${Arrays.toString(kalmanPrediction)}")
            println("Hasil Pengukuran: ${Arrays.toString(rssiValues)}")

            // Bandingkan hasil prediksi dengan posisi aktual
            val estimatedPosition = Position(kalmanPrediction[0], kalmanPrediction[1])
            val actualPosition = floorNode.position
            val error = calculateDistanceError(estimatedPosition, actualPosition)
            println("Estimasi Posisi: (${estimatedPosition.x}, ${estimatedPosition.y})")
            println("Posisi Aktual: (${actualPosition.x}, ${actualPosition.y})")
            println("Error: $error meter")

            // Jika error kurang dari 0.5 meter, maka anggap sebagai posisi yang tepat
            if (error < 0.5) {
                currentFloor = floorNodeId
                break
            }
        }
    }

    println("Pengguna berada di lantai $currentFloor")
    val textToSpeechMessage = "Anda berada di lantai $currentFloor, tujuan akhir Anda adalah $finalDestination"
    textToSpeechHelper.playText(textToSpeechMessage)
}

fun calculateDistanceError(estimatedPosition: Position, actualPosition: Position): Double {
    val deltaX = estimatedPosition.x - actualPosition.x
    val deltaY = estimatedPosition.y - actualPosition.y
    return sqrt(deltaX.pow(2) + deltaY.pow(2))
}

fun getFinalDestinationFromVoiceInput(recognizer: Recognizer): String {
    val finalDestinations = mutableListOf<String>()
    var recognitionResult: String? = null

    while (recognitionResult != "stop") {
        val partialResult = recognizer.result()
        if (partialResult.isFinal) {
            recognitionResult = partialResult.hyp()
            if (recognitionResult != null) {
                finalDestinations.add(recognitionResult)
            }
        }
    }

    return finalDestinations.lastOrNull() ?: "Tujuan tidak diketahui"
}

import android.content.Context
import android.view.accessibility.AccessibilityManager
import org.vosk.android.Recognizer
import org.vosk.android.SpeechService
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs

fun main() {
    val context: Context = TODO() // Pastikan Anda telah mengatur Context sesuai dengan lingkungan Anda
    val textToSpeechHelper = TextToSpeechHelper(context)
    textToSpeechHelper.initializeTTS()

    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val modelPath = TODO() // Path ke model Vosk yang telah diunduh
    val voskModel = VoskModel(modelPath)
    voskModel.loadModel()
    val voskRecognizer = VoskRecognizer(voskModel.getModel(), 16000.0f)
    voskRecognizer.startRecognizer()

    val finalDestination = getFinalDestinationFromVoiceInput(voskRecognizer.getRecognizer())
    println("Tujuan akhir pengguna adalah: $finalDestination")

    val initialPosition = doubleArrayOf(0.0, 0.0)
    val initialCovariance = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))
    val rssiDao: RssiDao = RoomRssiDao()
    var currentFloor = 1
    val finalPosition = Position(3.0, 1.0)
    val startNodeId = FloorGraph.floorGraph[currentFloor]?.firstOrNull { it.position == Position(initialPosition[0], initialPosition[1]) }?.id ?: 1
    val targetNodeId = FloorGraph.floorGraph[2]?.firstOrNull { it.position == finalPosition }?.id ?: 6
    val shortestPath = FloorGraph.findShortestPath(startNodeId, targetNodeId)
    val kalmanFilter = KalmanFilter(initialPosition, initialCovariance)

    for (i in 0 until shortestPath.size) {
        val currentNode = shortestPath[i]
        val rssiMeasurements = rssiDao.getRssiMeasurementsForFloor(currentFloor)
        val measurement = rssiMeasurements.map { 10.0.pow((-60 - it.rssiValue) / 20) }.toDoubleArray()
        val predictedPosition = kalmanFilter.predict()
        val updatedPosition = kalmanFilter.update(measurement)
        val nearestBeacon = rssiMeasurements.minByOrNull { it.rssiValue }
        val distanceToNearestBeacon = sqrt((updatedPosition[0] - nearestBeacon?.beaconId ?: 0).pow(2) + (updatedPosition[1] - nearestBeacon?.beaconId ?: 0).pow(2))
        val threshold = 0.1
        val deltaX = updatedPosition[0] - predictedPosition[0]
        val deltaY = updatedPosition[1] - predictedPosition[1]

        val textToRead: String = when {
            abs(deltaX) > threshold -> "Pengguna bergerak ${if (deltaX > 0) "ke kanan" else "ke kiri"}. Jarak ke BLE terdekat adalah ${String.format("%.2f", distanceToNearestBeacon)} meter."
            abs(deltaY) > threshold -> "Pengguna bergerak ${if (deltaY > 0) "maju" else "mundur"}. Jarak ke BLE terdekat adalah ${String.format("%.2f", distanceToNearestBeacon)} meter."
            else -> "Pengguna berada di tempat. Jarak ke BLE terdekat adalah ${String.format("%.2f", distanceToNearestBeacon)} meter."
        }

        println(textToRead)

        if (accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled) {
            textToSpeechHelper.playText(textToRead)
        }

        initialPosition[0] = updatedPosition[0]
        initialPosition[1] = updatedPosition[1]

        val currentNodeFloor = FloorGraph.floorGraph.find { it.value.any { node -> node.id == currentNode.id } }
        currentFloor = currentNodeFloor?.key ?: currentFloor
    }

    val textToReadFinal = "Pengguna telah mencapai tujuan akhir di lantai 2."
    println(textToReadFinal)

    if (accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled) {
        textToSpeechHelper.playText(textToReadFinal)
    }

    val distanceToFinalDestination = calculateDistanceToFinalDestination(initialPosition, finalPosition)
    println("Jarak ke tujuan akhir: $distanceToFinalDestination meter")

    val guidanceMessage = "Tujuan akhir Anda berjarak sekitar ${String.format("%.2f", distanceToFinalDestination)} meter. Terus maju ke arah itu."
    println(guidanceMessage)

    if (accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled) {
        textToSpeechHelper.playText(guidanceMessage)
    }
}

private fun getFinalDestinationFromVoiceInput(recognizer: Recognizer): String {
    val voskResult = SpeechService.getRecognizerResult(recognizer)
    return voskResult.text
}

private fun calculateDistanceToFinalDestination(initialPosition: DoubleArray, finalPosition: Position): Double {
    val deltaX = initialPosition[0] - finalPosition.x
    val deltaY = initialPosition[1] - finalPosition.y
    return sqrt(deltaX.pow(2) + deltaY.pow(2))
}
