/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.debug_console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.services.debug.DebugMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugConsoleViewModel @Inject constructor(private val blueManager: BlueManager) :
    ViewModel() {

    val debugMessages = MutableStateFlow<List<DebugMessage>>(emptyList())

    fun sendDebugMessage(deviceId: String, msg: String) {
        viewModelScope.launch {
            debugMessages.tryEmit(emptyList())
            blueManager.writeDebugMessage(
                nodeId = deviceId,
                msg = "$msg\n"
            )
        }
    }

    fun receiveDebugMessage(deviceId: String) {
        viewModelScope.launch {
            blueManager.getDebugMessages(nodeId = deviceId)?.collect {
                debugMessages.tryEmit(debugMessages.value + it)
            }
        }
    }
}