/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.debug_console

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import com.st.demo.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DebugConsole(
    navController: NavHostController,
    viewModel: DebugConsoleViewModel,
    deviceId: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        var queryState by rememberSaveable { mutableStateOf("") }
        val debugMessages by viewModel.debugMessages.collectAsState(initial = emptyList())
        val keyboardController = LocalSoftwareKeyboardController.current

        val annotatedString: AnnotatedString = buildAnnotatedString {
            debugMessages.forEach {
                withStyle(style = SpanStyle(if (it.isError) Color.Red else MaterialTheme.colors.onSurface)) {
                    append(it.payload)
                }
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            text = annotatedString
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = queryState,
            label = { Text(text = stringResource(R.string.st_debugConsole_hint)) },
            onValueChange = { queryState = it },
            trailingIcon = {
                IconButton(onClick = {
                    viewModel.sendDebugMessage(deviceId = deviceId, msg = queryState)
                    queryState = ""
                    keyboardController?.hide()
                }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Command"
                    )
                }
            },
        )
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.receiveDebugMessage(deviceId = deviceId)
    }
}