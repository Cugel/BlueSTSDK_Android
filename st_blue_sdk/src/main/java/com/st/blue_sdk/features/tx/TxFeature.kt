package com.st.blue_sdk.features.tx

import android.util.Log
import com.st.blue_sdk.LoggableUnit
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.ota.ImageInfo
import com.st.blue_sdk.features.rx.RxFeature
import com.st.blue_sdk.utils.NumberConversion

class TxFeature(
    name: String = NAME,
    type: Type = Type.EXTERNAL_ARTAINO,
    isEnabled: Boolean,
    identifier: Int,
) : Feature<ReceiveData>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = true
) {

    companion object {
        const val NAME = "TxFeature"
        val TAG = TxFeature::class.simpleName
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ReceiveData> {
        var readData = 0
        val availableData = data.size - dataOffset

        Log.d(TAG, "extractData, available: " + availableData.toString())

        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = readData, data = ReceiveData(data)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        Log.d(TxFeature.TAG, "packCommandData invoked");
        return null
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        Log.d(TxFeature.TAG, "parseCommandResponse invoked");
        return null
    }
}
