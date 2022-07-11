/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.switch

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.switch.request.SwitchOff
import com.st.blue_sdk.features.switch.request.SwitchOn
import com.st.blue_sdk.features.switch.response.SwitchOnOffResponse
import com.st.blue_sdk.utils.NumberConversion

class Switch(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<SwitchInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Switch"
        const val COMMAND_SWITCH_ON: Byte = 0x01
        const val COMMAND_SWITCH_OFF: Byte = 0x00
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<SwitchInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val switch = SwitchInfo(
            status = FeatureField(
                value = getStatusType(NumberConversion.byteToUInt8(data, dataOffset)),
                name = "Status"
            )
        )
        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = 1, data = switch
        )
    }

    private fun getStatusType(status: Short) = when (status.toInt()) {
        0x00 -> SwitchStatusType.Off
        0x01 -> SwitchStatusType.On
        else -> SwitchStatusType.Error
    }

    override fun packCommandData(
        featureBit: Int?,
        command: FeatureCommand
    ): ByteArray? {// TODO: To Be Checked
        return when (command) {
            is SwitchOn -> packCommandRequest(
                featureBit,
                COMMAND_SWITCH_ON,
                byteArrayOf()
            )
            is SwitchOff -> packCommandRequest(
                featureBit,
                COMMAND_SWITCH_OFF,
                byteArrayOf()
            )
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return unpackCommandResponse(data)?.let {
            if (mask != it.featureMask) return null

            val status = getStatusType(NumberConversion.byteToUInt8(data))
            SwitchOnOffResponse(
                feature = this,
                commandId = it.commandId,
                status = status
            )
        }
    }
}