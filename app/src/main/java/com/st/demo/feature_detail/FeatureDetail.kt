/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.feature_detail

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@SuppressLint("MissingPermission")
@Composable
fun FeatureDetail(
    navController: NavHostController,
    viewModel: FeatureDetailViewModel,
    deviceId: String,
    featureName: String
) {
    val backHandlingEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.startCalibration(deviceId, featureName)
    }

    BackHandler(enabled = backHandlingEnabled) {
        viewModel.disconnectFeature(deviceId = deviceId, featureName = featureName)

        navController.popBackStack()
    }

    val features = viewModel.featureUpdates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val isLogCatLoggerEnabled = viewModel.isLogCatLoggerEnabled(deviceId = deviceId)
            Column(modifier = Modifier.weight(0.3f)) {
                Text("Logger LogCat")
                Switch(checked = isLogCatLoggerEnabled, onCheckedChange = {
                    viewModel.enableDisableLogCatLogger(deviceId = deviceId, newState = it)
                })
            }
            val isCsvLoggerEnabled = viewModel.isCsvLoggerEnabled(deviceId = deviceId)
            Column(modifier = Modifier.weight(0.3f)) {
                Text("Logger CSV")
                Switch(checked = isCsvLoggerEnabled, onCheckedChange = {
                    viewModel.enableDisableCsvLogger(deviceId = deviceId, newState = it)
                })
            }
            val isDbLoggerEnabled = viewModel.isDbLoggerEnabled(deviceId = deviceId)
            Column(modifier = Modifier.weight(0.3f)) {
                Text("Logger DB")
                Switch(checked = isDbLoggerEnabled, onCheckedChange = {
                    viewModel.enableDisableDbLogger(deviceId = deviceId, newState = it)
                })
            }
        }

        Text("Name: $featureName")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Updates:")

        Spacer(modifier = Modifier.height(8.dp))

        Text("${features.value}")
    }

    LaunchedEffect(true) {
        viewModel.observeFeature(deviceId = deviceId, featureName = featureName)
        viewModel.sendExtendedCommand(featureName = featureName, deviceId = deviceId)
    }
}