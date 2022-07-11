/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OptionByte(
    @SerialName(value = "format")
    val format: String,
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "type")
    val type: String? = null,
    @SerialName(value = "negative_offset")
    val negative_offset: Int? = null,
    @SerialName(value = "scale_factor")
    val scale_factor: Int? = null,
    @SerialName(value = "string_values")
    val stringValues: List<OptionByteEnumType>? = null,
    @SerialName(value = "icon_values")
    val iconValues: List<OptionByteEnumType>? = null,
    @SerialName(value = "escape_value")
    val escapeValue: Int? = null,
    @SerialName(value = "escape_message")
    val escapeMessage: String? = null
)
