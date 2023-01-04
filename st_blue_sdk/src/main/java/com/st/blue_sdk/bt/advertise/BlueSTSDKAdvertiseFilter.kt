/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import android.util.Log
import android.util.SparseArray
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.utils.NumberConversion

private const val TAG = "BSTSDKAF"

class BlueSTSDKAdvertiseFilter : AdvertiseFilter {

    companion object {
        private const val VERSION_PROTOCOL_SUPPORTED_MIN: Byte = 0x01
        private const val VERSION_PROTOCOL_SUPPORTED_MAX: Byte = 0x02
        private const val DEVICE_NAME_TYPE: Byte = 0x09
        private const val TX_POWER_TYPE: Byte = 0x0A
        private const val VENDOR_DATA_TYPE: Byte = 0xFF.toByte()
    }

    override fun decodeAdvertiseData(advertisingData: ByteArray): BleAdvertiseInfo? {

        val splitAdv: SparseArray<ByteArray> = AdvertiseParser.split(advertisingData)

        var data = splitAdv.get(TX_POWER_TYPE.toInt())
        val txPower = data?.let { it[0] } ?: 0

        data = splitAdv[DEVICE_NAME_TYPE.toInt()]
        val name = data?.let { String(it) } ?: ""

        data = splitAdv[VENDOR_DATA_TYPE.toInt()]
        data?.let {

            var offset = 0

            if (it.size != 6 && it.size != 12 && it.size != 14) {
                return null
            }

            if (it.size == 14 && it[0].toInt() != 0x30 && it[1].toInt() != 0x00) {
                return null
            } else if (it.size == 14) {
                offset = 2
            }

            val protocolVersion: Short = NumberConversion.byteToUInt8(it, offset)
            
            if (protocolVersion < 0xee) {
                if (protocolVersion < VERSION_PROTOCOL_SUPPORTED_MIN || protocolVersion > VERSION_PROTOCOL_SUPPORTED_MAX) {
                    return null
                }
            } else if (protocolVersion > 0xee) {
                return null
            }

            val deviceId =
                if (it[1 + offset].toInt() and 0x80 == 0x80) it[1 + offset].toInt() and 0xFF
                else it[1 + offset].toInt() and 0x1F

            val model: Boards.Model = Boards.getModelFromIdentifier(deviceId)

            val isSleeping = getNodeSleepingState(it[1 + offset])
            val hasGeneralPurpose = getHasGenericPurposeFeature(it[1 + offset])
            val featureMap = NumberConversion.BigEndian.bytesToUInt32(it, 2 + offset)

            val address = if (it.size != 6) {
                String.format(
                    "%02X:%02X:%02X:%02X:%02X:%02X",
                    it[6 + offset], it[7 + offset], it[8 + offset],
                    it[9 + offset], it[10 + offset], it[11 + offset]
                )
            } else null

            Log.d(TAG, "Model:" + model + " is: "+ isSleeping + " gp: " + hasGeneralPurpose + " Feature map: " + featureMap.toString() + " Address: "+ address)

            return BlueStSdkAdvertiseInfo(
                name,
                txPower,
                address,
                featureMap,
                deviceId.toByte(),
                protocolVersion,
                model,
                isSleeping,
                hasGeneralPurpose
            )
        }

        return null
    }

    /**
     * parse the node type field to check if board is sleeping
     *
     * @param nodeType node type field
     * @return boolean false running true is sleeping
     */
    private fun getNodeSleepingState(nodeType: Byte): Boolean {
        return (nodeType.toInt() and 0x80) != 0x80 &&
                (nodeType.toInt() and 0x40) == 0x40
    }

    /**
     * parse the node type field to check if board has generic purpose implemented
     *
     * @param nodeType node type field
     * @return boolean false if the device has Generic purpose servicess and char
     */
    private fun getHasGenericPurposeFeature(nodeType: Byte): Boolean {
        return (nodeType.toInt() and 0x80) != 0x80 &&
                (nodeType.toInt() and 0x20) == 0x20
    }
}