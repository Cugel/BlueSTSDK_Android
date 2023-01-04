package com.st.blue_sdk.features.tx

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable

class ReceiveData(
    val data: ByteArray
) : Loggable {
    override val logHeader: String = "Data "
    override val logValue: String = data.toString()
}