/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

package com.st.blue_sdk.board_catalog.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @ColumnInfo(name = "dtmi")
    @SerialName("dtmi")
    val dtmi: String?=null,
    @ColumnInfo(name = "cloud_apps")
    @SerialName(value = "cloud_apps")
    val cloudApps: List<CloudApp>,
    @ColumnInfo(name = "characteristics")
    @SerialName(value = "characteristics")
    val characteristics: List<BleCharacteristic>,
    @ColumnInfo(name = "option_bytes")
    @SerialName(value = "option_bytes")
    val optionBytes: List<OptionByte>,
    @ColumnInfo(name = "fw_desc")
    @SerialName("fw_desc")
    val fw_desc: String,
    @ColumnInfo(name = "changelog")
    @SerialName("changelog")
    val changelog: String?=null,
    @ColumnInfo(name = "fota")
    @SerialName("fota")
    var fota: FotaDetails,
)
