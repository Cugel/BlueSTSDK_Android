package com.st.blue_sdk.features.rx

import android.util.Log
import com.st.blue_sdk.LoggableUnit
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate

class RxFeature(
    name: String = NAME,
    type: Type = Type.EXTERNAL_STM32,
    isEnabled: Boolean,
    identifier: Int
) : Feature<LoggableUnit>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false
) {
    companion object {
        const val NAME = "RxFeature"
        val TAG = RxFeature::class.simpleName
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LoggableUnit> {
        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = byteArrayOf(),
            readByte = 0,
            data = LoggableUnit()
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is SendData -> command.payload
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        Log.d(TAG, "parseCommandResponse invoked");
        return null
    }
}