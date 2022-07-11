/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.st.blue_sdk.services.ota.FwUploadError
import kotlin.math.roundToInt

@Composable
fun FwUpdateProgressDialog(
    modifier: Modifier = Modifier,
    progress: Float?
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { },
        buttons = {},
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(modifier = modifier.padding(end = 16.dp))
                Text(text = "Progress:\n"+String.format("%.2f", progress?: 0.0) +"%")
            }
        }
    )
}


@Composable
fun FwUpgradeErrorDialog(
    modifier: Modifier = Modifier,
    fwUploadError: FwUploadError,
    onPositiveButtonPressed: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { },
        text = {
            Text(text = "FwUploadError: ${fwUploadError.name}")
        },
        confirmButton = {
            Button(onClick = { onPositiveButtonPressed() }) {
                Text(text = "Ok")
            }
        })
}