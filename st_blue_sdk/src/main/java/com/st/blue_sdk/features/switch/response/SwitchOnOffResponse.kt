/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.switch.response

import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.switch.Switch
import com.st.blue_sdk.features.switch.SwitchStatusType

class SwitchOnOffResponse(feature: Switch, commandId: Byte, val status: SwitchStatusType) :
    FeatureResponse(feature = feature, commandId = commandId)