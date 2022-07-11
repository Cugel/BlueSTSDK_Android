/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import java.util.*

private const val BLE_MIN_MTU = 23 //default ble length (23-3=20 ==max packet length)
private const val BLE_PACKET_HEADER_LEN = 3

data class Node(
    val device: BluetoothDevice,
    val advertiseInfo: BleAdvertiseInfo?,
    val rssi: RssiData? = null,
    val mtu: Int = BLE_MIN_MTU,
    val connectionStatus: ConnectionStatus = ConnectionStatus(),
    val deviceGatt: BluetoothGatt? = null
) {

    val maxPayloadSize = mtu - BLE_PACKET_HEADER_LEN

    val friendlyName: String
        get() {
            val tag = if (device.address.isNullOrEmpty()) {
                "NA"
            } else {
                device.address.replace(":", "").takeLast(6)
            }
            return "${advertiseInfo?.getName()} @${tag}"
        }
}

data class ConnectionStatus(
    val prev: NodeState = NodeState.Disconnected,
    val current: NodeState = NodeState.Disconnected
)

data class RssiData(val rssi: Int, val timestamp: Date)