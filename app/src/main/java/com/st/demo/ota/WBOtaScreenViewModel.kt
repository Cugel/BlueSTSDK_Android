/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.ota

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.services.ota.FirmwareType
import com.st.blue_sdk.services.ota.FwFileDescriptor
import com.st.blue_sdk.services.ota.FwUpdateListener
import com.st.blue_sdk.services.ota.FwUploadError
import com.st.blue_sdk.services.ota.characteristic.CharacteristicFwUpgrade
import com.st.blue_sdk.utils.WbOTAUtils
import com.st.demo.models.FwUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WBOtaScreenViewModel @Inject constructor(
    private val application: Application,
    private val blueManager: BlueManager
) : ViewModel() {

    companion object {
        private val TAG = WBOtaScreenViewModel::class.simpleName
    }

    val fwUpdateState = mutableStateOf(FwUpdateState())

    fun startWBFwUpgrade(
        deviceId: String,
        fileUri: Uri,
        firmwareType: FirmwareType,
        boardType: WbOTAUtils.WBBoardType
    ) {
        fwUpdateState.value = fwUpdateState.value.copy(isInProgress = true)
        viewModelScope.launch {

            val fileDescriptor =
                FwFileDescriptor(fileUri = fileUri, resolver = application.contentResolver)

            val params = CharacteristicFwUpgrade.buildFwUpgradeParams(
                firmwareType = firmwareType,
                boardType = boardType,
                fileDescriptor = fileDescriptor
            )

            blueManager.upgradeFw(nodeId = deviceId)?.launchFirmwareUpgrade(
                nodeId = deviceId,
                fwType = FirmwareType.BOARD_FW,
                fileDescriptor = fileDescriptor,
                params = params,
                fwUpdateListener = otaListener
            )
        }
    }

    fun clearFwUpdateState() {
        fwUpdateState.value = FwUpdateState()
    }

    private val otaListener = object : FwUpdateListener {

        override fun onUpdate(progress: Float) {
            Log.d(TAG, "update progress $progress")
            fwUpdateState.value = fwUpdateState.value.copy(isInProgress = true, progress = progress)
        }

        override fun onComplete() {
            Log.d(TAG, "COMPLETE")
            clearFwUpdateState()
        }

        override fun onError(error: FwUploadError) {
            fwUpdateState.value =
                fwUpdateState.value.copy(isInProgress = false, progress = null, error = error)
        }
    }
}