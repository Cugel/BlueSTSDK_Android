/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.nrg.response

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable

class ImageTUContentInfo(
    val expectedWriteLength: FeatureField<Int>
) : Loggable {

    override val logHeader: String
        get() = expectedWriteLength.logHeader

    override val logValue: String
        get() = expectedWriteLength.logValue
}