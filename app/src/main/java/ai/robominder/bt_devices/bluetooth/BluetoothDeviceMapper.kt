package ai.robominder.bt_devices.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(type: BTType): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address,
        type = type
    )
}