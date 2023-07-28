import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityManager
import java.util.*

class TextToSpeechHelper(private val context: Context) {
    private lateinit var textToSpeech: TextToSpeech
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.US
            }
        }
    }

    fun playText(text: String) {
        if (accessibilityManager.isTouchExplorationEnabled) {
            // TalkBack aktif, baca teks menggunakan TextToSpeech
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            // Jika TalkBack tidak aktif, cukup cetak teks ke konsol
            println(text)
        }
    }
}