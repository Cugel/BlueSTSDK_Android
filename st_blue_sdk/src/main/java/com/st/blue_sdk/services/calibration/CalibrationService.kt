/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

package com.st.blue_sdk.services.calibration

import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.Feature

interface CalibrationService {

    suspend fun startCalibration(feature: Feature<*>, nodeId: String): CalibrationStatus
}