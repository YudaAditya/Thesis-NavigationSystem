// RssiDao.kt
data class RssiMeasurement(val floorNumber: Int, val beaconId: Int, val rssiValue: Double)

interface RssiDao {
    fun getRssiMeasurementsForFloor(floorNumber: Int): List<RssiMeasurement>
}