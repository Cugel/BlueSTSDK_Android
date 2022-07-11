/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.exported

import java.util.*

class AudioOpusMusicFeature(
    override val name: String = NAME,
    override val isEnabled: Boolean = true,
    override val uuid: UUID = UUID.fromString(AudioOpusConfFeature.AUDIO_OPUS_CONF_UUID)
) : ExportedFeature {

    companion object {
        const val AUDIO_OPUS_MUSIC_UUID = "00000011-0002-11e1-ac36-0002a5d5c51b"
        const val NAME = "AudioOpusMusicFeature"
    }

    override fun copy(isEnabled: Boolean): ExportedFeature =
        AudioOpusMusicFeature(isEnabled = isEnabled)
}