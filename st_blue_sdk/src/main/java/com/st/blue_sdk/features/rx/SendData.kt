package com.st.blue_sdk.features.rx

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand

class SendData (feature: Feature<*>, commandId: Byte, val payload: ByteArray) :
    FeatureCommand(feature, commandId)
