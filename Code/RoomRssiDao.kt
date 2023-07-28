class RoomRssiDao : RssiDao {
    // Contoh data RSSI dari perangkat BLE dengan jumlah tidak terhitung
    private val rssiMeasurements = mutableListOf(
        RssiMeasurement(1, 1, -60.0),
        RssiMeasurement(1, 2, -70.0),
        RssiMeasurement(1, 3, -50.0),
        RssiMeasurement(2, 4, -60.0),
        RssiMeasurement(2, 5, -70.0),
        RssiMeasurement(2, 6, -50.0)
    )

    override fun getRssiMeasurementsForFloor(floorNumber: Int): List<RssiMeasurement> {
        // Filter data RSSI untuk mendapatkan data dari BLE yang sesuai dengan lantai saat ini
        return rssiMeasurements.filter { it.floorNumber == floorNumber }
    }
}