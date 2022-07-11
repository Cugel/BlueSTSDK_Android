/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.models.Boards
import java.util.*

fun BluetoothGattCharacteristic.isStandardFeatureCharacteristics() =
    uuid.toString().endsWith(Feature.Type.STANDARD.suffix)

fun BluetoothGattCharacteristic.isGeneralPurposeFeatureCharacteristics() =
    uuid.toString().endsWith(Feature.Type.GENERAL_PURPOSE.suffix)

fun BluetoothGattCharacteristic.isExtendedOrExternalFeatureCharacteristics() =
    uuid.toString().endsWith(Feature.Type.EXTENDED.suffix) ||
            uuid.toString().endsWith(Feature.Type.EXTERNAL_STM32.suffix) ||
            uuid.toString().endsWith(Feature.Type.EXTERNAL_BLUE_NRG_OTA.suffix) ||
            uuid.toString().endsWith(Feature.Type.EXTERNAL_STD_CHART.suffix)

private const val SHR_MASK = 32

fun BluetoothGattCharacteristic.getFeature(): Feature<*> {
    val header = uuid.toString().substring(0..7)
    val suffix = uuid.toString().substring(8)

    return Feature.createFeature(
        identifier = Integer.decode("0x$header"),
        type = Feature.Type.fromSuffix(suffix = suffix)
    )
}

fun BluetoothGattCharacteristic.getGPFeature(): Feature<*> {
    val header = uuid.toString().substring(0..3)
    val suffix = uuid.toString().substring(4)

    return Feature.createFeature(
        identifier = Integer.decode("0x$header"),
        type = Feature.Type.fromSuffix(suffix = suffix)
    )
}

fun BluetoothGattCharacteristic.buildFeatures(
    advertiseMask: Long,
    protocolVersion: Short,
    boardModel: Boards.Model
): List<Feature<*>> {
    val featureMask = (uuid.mostSignificantBits shr SHR_MASK).toInt()
    val features = mutableListOf<Feature<*>>()

    var mask = 1L shl 31
    for (i in 0..31) {
        if ((featureMask and mask.toInt()) != 0) {
            features.add(
                Feature.createFeature(
                    boardModel = boardModel,
                    type = Feature.Type.STANDARD,
                    identifier = mask.toInt(),
                    isEnabled =
                    if (protocolVersion.toInt() == 1) {
                        advertiseMask and mask != 0L
                    } else {
                        true
                    }
                )
            )
        }
        mask = mask shr 1
    }

    return features
}

fun UUID.buildNotifiableChar(uuid: UUID): BluetoothGattCharacteristic {
    val gattChar =
        BluetoothGattCharacteristic(this, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0)
    val aDesc =
        BluetoothGattDescriptor(uuid, BluetoothGattDescriptor.PERMISSION_WRITE)
    aDesc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    gattChar.addDescriptor(aDesc)
    return gattChar
}
