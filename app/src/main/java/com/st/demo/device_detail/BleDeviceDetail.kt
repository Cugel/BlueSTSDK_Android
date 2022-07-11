/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.device_detail

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.st.blue_sdk.models.NodeState
import com.st.blue_sdk.services.ota.UpgradeStrategy
import com.st.demo.R
import com.st.demo.composables.FwUpdateProgressDialog
import com.st.demo.composables.FwUpgradeErrorDialog

@SuppressLint("MissingPermission")
@Composable
fun BleDeviceDetail(
    navController: NavHostController,
    viewModel: BleDeviceDetailViewModel,
    deviceId: String
) {
    LaunchedEffect(key1 = deviceId) {
        viewModel.connect(deviceId = deviceId)
    }

    val bleDevice = viewModel.bleDevice(deviceId = deviceId).collectAsState(null)
    val features = viewModel.features.collectAsState()

    if (bleDevice.value?.connectionStatus?.current == NodeState.Ready) {
        viewModel.getFeatures(deviceId = deviceId)
    }

    val backHandlingEnabled by remember { mutableStateOf(true) }

    BackHandler(enabled = backHandlingEnabled) {
        viewModel.disconnect(deviceId = deviceId)

        navController.popBackStack()
    }

    if (viewModel.fwUpdateState.value.isInProgress) {
        FwUpdateProgressDialog(progress = viewModel.fwUpdateState.value.progress)
    }

    if (viewModel.fwUpdateState.value.error != null) {
        FwUpgradeErrorDialog(fwUploadError = viewModel.fwUpdateState.value.error!!) {
            viewModel.clearFwUpdateState()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .scrollable(
                state = scrollState,
                orientation = Orientation.Vertical
            )
    ) {

        val pickFileLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { fileUri ->
            if (fileUri != null) {
                viewModel.startUpgradeFW(
                    deviceId = deviceId,
                    fileUri = fileUri
                )
            }
        }

        Row(modifier = Modifier.align(End)) {
            IconButton(onClick = {
                navController.navigate("debug/${deviceId}")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_bug_report_24),
                    contentDescription = "Debug Console"
                )
            }
            IconButton(onClick = {
                if (viewModel.getFwUpdateStrategy(deviceId) == UpgradeStrategy.CHARACTERISTIC) {
                    navController.navigate("ota/${deviceId}")
                    return@IconButton
                }
                pickFileLauncher.launch(arrayOf("*/*"))
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_upload_file_24),
                    contentDescription = "Upgrade FW"
                )
            }
            IconButton(
                enabled = bleDevice.value?.connectionStatus?.current?.equals(NodeState.Ready)
                    ?: false,
                onClick = {
                    navController.navigate("audio/${deviceId}")
                }) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "Test Audio")
            }
        }

        Text("Name: ${bleDevice.value?.device?.name ?: ""}")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Status: ${bleDevice.value?.connectionStatus?.current?.name?.uppercase() ?: ""}")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Features: ")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            val items = features.value.filter { it.isDataNotifyFeature }
            itemsIndexed(items = items) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                navController.navigate("feature/${deviceId}/${item.name}")
                            },
                        text = item.name,
                    )
                }
                if (items.lastIndex != index) {
                    Divider()
                }
            }
        }
    }
}
