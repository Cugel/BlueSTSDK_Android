/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.demo.MainActivity.Companion.ACCEPTED
import com.st.demo.MainActivity.Companion.LA_ACCEPTED
import com.st.demo.audio.AudioScreen
import com.st.demo.debug_console.DebugConsole
import com.st.demo.device_detail.BleDeviceDetail
import com.st.demo.device_list.BleDeviceList
import com.st.demo.feature_detail.FeatureDetail
import com.st.demo.license_agreement.LicenseAgreementScreen
import com.st.demo.license_agreement.LicenseAgreementScreenViewModel
import com.st.demo.ota.WBOtaScreen
import com.st.demo.ui.theme.STBleSDKTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

    companion object {
        const val LA_ACCEPTED = "LA_ACCEPTED"
        const val ACCEPTED = "ACCEPTED"
    }
}

@Composable
private fun MainScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    STBleSDKTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {

            val sp = LocalContext.current.getSharedPreferences(LA_ACCEPTED, Context.MODE_PRIVATE)
            val startDestination = if(sp.getBoolean(ACCEPTED, false)) "list" else "license"

            NavHost(navController = navController, startDestination = startDestination) {

                composable(route = "license") {
                    LicenseAgreementScreen(
                        viewModel = LicenseAgreementScreenViewModel(sp),
                        navController = navController
                    )
                }

                composable(route = "list") {
                    BleDeviceList(
                        viewModel = hiltViewModel(),
                        navController = navController
                    )
                }

                composable(
                    route = "detail/{deviceId}",
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                        BleDeviceDetail(
                            viewModel = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId
                        )
                    }
                }

                composable(
                    route = "feature/{deviceId}/{featureName}",
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType },
                        navArgument("featureName") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                        backStackEntry.arguments?.getString("featureName")?.let { featureName ->
                            FeatureDetail(
                                viewModel = hiltViewModel(),
                                navController = navController,
                                deviceId = deviceId,
                                featureName = featureName
                            )
                        }
                    }
                }

                composable(
                    route = "debug/{deviceId}",
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("deviceId")
                        ?.let { deviceId ->
                            DebugConsole(
                                viewModel = hiltViewModel(),
                                navController = navController,
                                deviceId = deviceId
                            )
                        }
                }

                composable(
                    route = "ota/{deviceId}",
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                        WBOtaScreen(
                            viewModel = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId
                        )
                    }
                }

                composable(
                    route = "audio/{deviceId}",
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                        AudioScreen(
                            viewModel = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId
                        )
                    }
                }
            }
        }
    }
}