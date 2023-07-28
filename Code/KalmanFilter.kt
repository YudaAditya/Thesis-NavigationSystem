class KalmanFilter(private val initialState: DoubleArray, private val initialCovariance: Array<DoubleArray>) {
    private var state: DoubleArray = initialState.copyOf()
    private var covariance: Array<DoubleArray> = initialCovariance.copyOf()

    fun predict(): DoubleArray {
        // Langkah 1: Prediksi nilai state selanjutnya berdasarkan model gerakan
        val transitionMatrix = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))
        state = multiplyMatrix(transitionMatrix, state)

        // Langkah 2: Prediksi kovarian state selanjutnya berdasarkan model gerakan dan proses noise
        val processNoise = arrayOf(doubleArrayOf(0.01, 0.0), doubleArrayOf(0.0, 0.01))
        covariance = addMatrices(multiplyMatrix(multiplyMatrix(transitionMatrix, covariance), transposeMatrix(transitionMatrix)), processNoise)

        return state
    }

    fun update(measurement: DoubleArray): DoubleArray {
        // Langkah 1: Hitung residual (perbedaan antara pengukuran aktual dan prediksi)
        val residual = subtractVectors(measurement, state)

        // Langkah 2: Hitung matriks H, yang menghubungkan pengukuran ke state
        val measurementMatrix = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))

        // Langkah 3: Hitung error kovarian pengukuran
        val measurementNoise = arrayOf(doubleArrayOf(0.01, 0.0), doubleArrayOf(0.0, 0.01))
        val measurementCovariance = addMatrices(multiplyMatrix(multiplyMatrix(measurementMatrix, covariance), transposeMatrix(measurementMatrix)), measurementNoise)

        // Langkah 4: Hitung matriks Kalman Gain
        val kalmanGain = multiplyMatrix(covariance, multiplyMatrix(transposeMatrix(measurementMatrix), inverseMatrix(measurementCovariance)))

        // Langkah 5: Perbarui state dan kovarian berdasarkan pengukuran aktual
        state = addVectors(state, multiplyMatrix(kalmanGain, residual))
        covariance = multiplyMatrix(subtractMatrices(createIdentityMatrix(2), multiplyMatrix(kalmanGain, measurementMatrix)), covariance)

        return state
    }

    // Fungsi bantuan untuk menghitung perkalian dua matriks
    private fun multiplyMatrix(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val result = Array(a.size) { DoubleArray(b[0].size) { 0.0 } }
        for (i in a.indices) {
            for (j in b[0].indices) {
                for (k in b.indices) {
                    result[i][j] += a[i][k] * b[k][j]
                }
            }
        }
        return result
    }

    // Fungsi bantuan untuk menghitung perkalian matriks dengan vektor
    private fun multiplyMatrix(a: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        val result = DoubleArray(a.size) { 0.0 }
        for (i in a.indices) {
            for (j in b.indices) {
                result[i] += a[i][j] * b[j]
            }
        }
        return result
    }

    // Fungsi bantuan untuk mengurangi dua vektor
    private fun subtractVectors(a: DoubleArray, b: DoubleArray): DoubleArray {
        val result = DoubleArray(a.size) { 0.0 }
        for (i in a.indices) {
            result[i] = a[i] - b[i]
        }
        return result
    }

    // Fungsi bantuan untuk menambah dua vektor
    private fun addVectors(a: DoubleArray, b: DoubleArray): DoubleArray {
        val result = DoubleArray(a.size) { 0.0 }
        for (i in a.indices) {
            result[i] = a[i] + b[i]
        }
        return result
    }

    // Fungsi bantuan untuk mengurangi dua matriks
    private fun subtractMatrices(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val result = Array(a.size) { DoubleArray(a[0].size) { 0.0 } }
        for (i in a.indices) {
            for (j in a[0].indices) {
                result[i][j] = a[i][j] - b[i][j]
            }
        }
        return result
    }

    // Fungsi bantuan untuk membuat matriks identitas
    private fun createIdentityMatrix(size: Int): Array<DoubleArray> {
        val matrix = Array(size) { DoubleArray(size) { 0.0 } }
        for (i in matrix.indices) {
            matrix[i][i] = 1.0
        }
        return matrix
    }

    // Fungsi bantuan untuk mentranspose matriks
    private fun transposeMatrix(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val result = Array(matrix[0].size) { DoubleArray(matrix.size) { 0.0 } }
        for (i in matrix.indices) {
            for (j in matrix[0].indices) {
                result[j][i] = matrix[i][j]
            }
        }
        return result
    }

    // Fungsi bantuan untuk menghitung invers matriks
    private fun inverseMatrix(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val determinant = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]
        val result = Array(matrix.size) { DoubleArray(matrix[0].size) { 0.0 } }
        result[0][0] = matrix[1][1] / determinant
        result[0][1] = -matrix[0][1] / determinant
        result[1][0] = -matrix[1][0] / determinant
        result[1][1] = matrix[0][0] / determinant
        return result
    }
}





