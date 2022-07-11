/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

@file:UseSerializers(BoardFotaTypeSerializer::class)

package com.st.blue_sdk.board_catalog.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.st.blue_sdk.board_catalog.api.serializers.BoardFotaTypeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Entity(
    primaryKeys = ["ble_dev_id", "ble_fw_id"],
    tableName = "board_firmware"
)
@Serializable
data class BoardFirmware(
    @ColumnInfo(name = "ble_dev_id")
    @SerialName(value = "ble_dev_id")
    val bleDevId: String,
    @ColumnInfo(name = "ble_fw_id")
    @SerialName(value = "ble_fw_id")
    val bleFwId: String,
    @ColumnInfo(name = "board_name")
    @SerialName(value = "brd_name")
    val brdName: String,
    @ColumnInfo(name = "fw_version")
    @SerialName(value = "fw_version")
    val fwVersion: String,
    @ColumnInfo(name = "fw_name")
    @SerialName(value = "fw_name")
    val fwName: String,
    @ColumnInfo(name = "fota")
    @SerialName(value = "fota")
    val fota: BoardFotaType,
    @ColumnInfo(name = "cloud_apps")
    @SerialName(value = "cloud_apps")
    val cloudApps: List<CloudApp>? = null,
    @ColumnInfo(name = "characteristics")
    @SerialName(value = "characteristics")
    val characteristics: List<BleCharacteristic>? = null,
    @ColumnInfo(name = "option_bytes")
    @SerialName(value = "option_bytes")
    val optionBytes: List<OptionByte>? = null,
    @ColumnInfo(name = "partial_fota")
    @SerialName(value = "partial_fota")
    val partialFota: Int=0,
    @SerialName("fota_max_chunk_length")
    @ColumnInfo(name = "fota_max_chunk_length")
    var fotaMaxChunkSize: Int? = null,
    @SerialName("fota_chunk_divisor_constraint")
    @ColumnInfo(name = "fota_chunk_divisor_constraint")
    var fotaChunkDivisorConstraint: Int? = null
)
