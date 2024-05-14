package ai.robominder.bt_devices.bluetooth

typealias BluetoothDeviceDomain = BluetoothDeviceItem

data class BluetoothDeviceItem(
    val name: String?,
    val address: String,
    val type: BTType
)

enum class BTType {
    FOUND, PAIRED
}