/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.hs_datalog_config.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SensorType {
    @SerialName("ACC")
    Accelerometer,

    @SerialName("MAG")
    Magnetometer,

    @SerialName("GYRO")
    Gyroscope,

    @SerialName("TEMP")
    Temperature,

    @SerialName("HUM")
    Humidity,

    @SerialName("PRESS")
    Pressure,

    @SerialName("MIC")
    Microphone,

    @SerialName("MLC")
    MLC,

    @SerialName("CLASS")
    CLASS,

    @SerialName("STREDL")
    STREDL
}
