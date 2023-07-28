import org.vosk.Recognizer
import org.vosk.android.AudioFile

class VoskRecognizer(private val model: Model, private val sampleRate: Float) {
    private lateinit var recognizer: Recognizer

    fun startRecognizer() {
        recognizer = AudioFileRecognizer(model, sampleRate)
    }

    fun getRecognizer(): Recognizer {
        return recognizer
    }
}
