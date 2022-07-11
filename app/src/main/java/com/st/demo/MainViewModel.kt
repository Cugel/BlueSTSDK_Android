/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo

import androidx.lifecycle.ViewModel
import com.st.blue_sdk.BlueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val blueManager: BlueManager,
) : ViewModel() {


    companion object {
        private val TAG = MainViewModel::class.simpleName
    }
}

