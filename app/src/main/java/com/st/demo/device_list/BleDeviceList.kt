/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
@file:OptIn(ExperimentalPermissionsApi::class)

package com.st.demo.device_list

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.st.demo.R

@SuppressLint("MissingPermission")
@Composable
fun BleDeviceList(
    viewModel: BleDeviceListViewModel,
    navController: NavHostController
) {

    val context = LocalContext.current

    var doNotShowRationale by rememberSaveable {
        mutableStateOf(false)
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        else
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
    )

    PermissionsRequired(
        multiplePermissionsState = locationPermissionState,
        permissionsNotGrantedContent = {
            if (doNotShowRationale) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Feature not available")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("The Location and Record Audio is important for this app. Please grant the permission.")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(modifier = Modifier.weight(0.5f),
                            onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                            Text("Ok!")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            modifier = Modifier.weight(0.5f),
                            onClick = { doNotShowRationale = true }) {
                            Text("Nope")
                        }
                    }
                }
            }
        },
        permissionsNotAvailableContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Location or Record Audio permission denied. See this FAQ with information about why we " +
                            "need this permission. Please, grant us access on the Settings screen."
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    }) {
                    Text("Open Settings")
                }
            }
        }
    ) {
        // remember calculates the value passed to it only during the first composition. It then
        // returns the same value for every subsequent composition. More details are available in the
        // comments below.
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
            val current = LocalContext.current
            val pickFileLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { fileUri ->
                if (fileUri != null) {
                    viewModel.setLocalBoardCatalog(
                        fileUri = fileUri,
                        contentResolver = current.contentResolver
                    )
                }
            }

            Row(modifier = Modifier.align(Alignment.End)) {
                IconButton(onClick = {
                    pickFileLauncher.launch(arrayOf("*/*"))
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_upload_file_24),
                        contentDescription = "Upgrade FW"
                    )
                }
                IconButton(onClick = {
                    navController.navigate("license")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_help_24),
                        contentDescription = "Show LA"
                    )
                }
            }

            Text(text = stringResource(R.string.st_deviceList_title))

            Spacer(modifier = Modifier.height(8.dp))

            val devices = viewModel.scanBleDevices.collectAsState(initial = emptyList())

            val isRefreshing by viewModel.isLoading.collectAsState()

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.startScan() },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(items = devices.value) { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("detail/${item.device.address}")
                                },
                            elevation = 10.dp
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = item.friendlyName
                            )
                        }

                        if (devices.value.lastIndex != index) {
                            Divider()
                        }
                    }
                }
            }
        }

        LaunchedEffect(key1 = Unit) {
            viewModel.startScan()
        }
    }
}