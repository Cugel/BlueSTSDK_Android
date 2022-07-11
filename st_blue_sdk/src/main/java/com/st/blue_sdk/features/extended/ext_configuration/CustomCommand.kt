/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ext_configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomCommand(
    @SerialName("Name")
    val name: String,
    @SerialName("Type")
    val type: String,
    @SerialName("Min")
    val min: Int = 0,
    @SerialName("Max")
    val max: Int = 1,
    @SerialName("Desc")
    val description: String?,
    @SerialName("StringValues")
    val stringValues: List<String>,
    @SerialName("IntegerValues")
    val integerValues: List<Int>
)