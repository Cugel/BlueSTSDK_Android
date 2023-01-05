package com.st.blue_sdk.services.bidt

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.bt.hal.BleHal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import java.nio.charset.StandardCharsets

class BIDTServiceImpl(
    private val bleHAL: BleHal
) : BIDTService {

    companion object {
        private val CHARSET = StandardCharsets.ISO_8859_1 //ASCII
        private val TAG = BIDTService::class.simpleName
    }

    private var rxCharacteristic: BluetoothGattCharacteristic? = null

    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var hasEnabledTxNotifications = false

    override suspend fun init() {

        if (hasBIDTService().not()) {
            bleHAL.getDiscoveredServices().forEach {
                Log.d(TAG, it.uuid.toString())
            }
            
            Log.e(TAG, "BIDT Service not supported")
            return
        }
        else

        Log.d(TAG, "BIDT Service IS supported")
            
        rxCharacteristic = bleHAL.getCharacteristic(
            NodeService.BIDT_SERVICE_UUID.toString(),
            NodeService.BIDT_RX_CHARACTERISTIC_UUID.toString()
        )

        txCharacteristic = bleHAL.getCharacteristic(
            NodeService.BIDT_SERVICE_UUID.toString(),
            NodeService.BIDT_TX_CHARACTERISTIC_UUID.toString()
        )

        enableTxNotifications()
    }

    override fun hasBIDTService(): Boolean {
        return bleHAL.getDiscoveredServices()
            .any { service -> service.uuid == NodeService.BIDT_SERVICE_UUID }
    }

    override suspend fun write(data: ByteArray, mtu: Int?): Int {

        rxCharacteristic ?: return 0

        enableTxNotifications()

        val payloadSize = mtu ?: bleHAL.getDevice().maxPayloadSize

        val hasWriteData = bleHAL.writeCharacteristic(
            characteristic = rxCharacteristic!!,
            data = data,
            payloadSize = payloadSize
        )
        return if (hasWriteData) data.size else 0
    }

    override fun getReadData(): Flow<ByteArray> {

        return bleHAL.getDeviceNotifications().transform {
            if (it.characteristic.uuid == txCharacteristic?.uuid) {
                emit(it.data)
            }
        }
    }

    override suspend fun read(timeout: Long): ByteArray? {
        return txCharacteristic?.let { recipient ->
            val data = bleHAL.readCharacteristic(recipient, timeout)
            return data?.let { payload -> data }
        }
    }

    override fun getMaxPayloadSize(): Int {
        return bleHAL.getDevice().maxPayloadSize
    }

    private suspend fun enableTxNotifications() {

        if (hasEnabledTxNotifications.not()) {
            txCharacteristic?.let {
                hasEnabledTxNotifications = bleHAL.setCharacteristicNotification(it, true)
                Log.d(TAG, "RW result $hasEnabledTxNotifications")
            }
        }
    }
}