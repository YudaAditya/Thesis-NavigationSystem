import org.vosk.Model

class VoskModel(private val modelPath: String) {
    private lateinit var model: Model

    fun loadModel() {
        model = Model(modelPath)
    }

    fun getModel(): Model {
        return model
    }
}
