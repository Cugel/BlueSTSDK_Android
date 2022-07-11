/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.ota

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.st.blue_sdk.services.ota.FirmwareType
import com.st.blue_sdk.utils.WbOTAUtils
import com.st.demo.composables.FwUpdateProgressDialog
import com.st.demo.composables.FwUpgradeErrorDialog

@Composable
fun WBOtaScreen(
    navController: NavHostController,
    viewModel: WBOtaScreenViewModel,
    deviceId: String,
    modifier: Modifier = Modifier,
) {

    var radioSelection by remember { mutableStateOf(0) }
    var selectedFileUri: Uri? by remember { mutableStateOf(null) }
    var selectedBoardIndex by remember { mutableStateOf(0) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        if (fileUri != null) {
            selectedFileUri = fileUri
        }
    }

    if (viewModel.fwUpdateState.value.isInProgress) {
        FwUpdateProgressDialog(progress = viewModel.fwUpdateState.value.progress)
    }

    if (viewModel.fwUpdateState.value.error != null) {
        FwUpgradeErrorDialog(fwUploadError = viewModel.fwUpdateState.value.error!!) {
            viewModel.clearFwUpdateState()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        BoardDropdown(modifier = modifier, selectedBoardIndex) { boardIndex ->
            selectedBoardIndex = boardIndex
        }

        val radioOptions = listOf("Application", "Wireless")
        radioOptions.forEachIndexed { index, text ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (index == radioSelection),
                        onClick = {
                            radioSelection = index
                        }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (index == radioSelection),
                    onClick = { radioSelection = index }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = { pickFileLauncher.launch(arrayOf("*/*")) }) {
            Text(text = "Select File")
        }

        Button(
            modifier = modifier.fillMaxWidth(),
            enabled = selectedFileUri != null,
            onClick = {
                selectedFileUri?.let {
                    viewModel.startWBFwUpgrade(
                        deviceId = deviceId,
                        fileUri = it,
                        boardType = if (selectedBoardIndex == 0) WbOTAUtils.WBBoardType.WB5xOrWB3x else WbOTAUtils.WBBoardType.WB1x,
                        firmwareType = if (radioSelection == 0) FirmwareType.BOARD_FW else FirmwareType.BLE_FW
                    )
                }
            }) {
            Text(text = "Start upload")
        }
    }
}

@Composable
fun BoardDropdown(
    modifier: Modifier,
    selectedIndex: Int = 0,
    onSelection: (Int) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    val items = listOf("STM32WB5x/WB3x", "STM32WB1x")

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            items[selectedIndex],
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .wrapContentHeight()
                .clickable(onClick = { expanded = true })
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    onSelection(index)
                    expanded = false
                }) {
                    Text(text = s)
                }
            }
        }
    }
}


