/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.std

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable

data class HeartRateInfo(
    val heartRate: FeatureField<Int>,
    val energyExpended: FeatureField<Int>,
    val rrInterval: FeatureField<Float>

) : Loggable {
    override val logHeader: String =
        "${heartRate.logHeader}, ${energyExpended.logHeader}, ${rrInterval.logHeader}"

    override val logValue: String =
        "${heartRate.logValue}, ${energyExpended.logValue}, ${rrInterval.logValue}"
}