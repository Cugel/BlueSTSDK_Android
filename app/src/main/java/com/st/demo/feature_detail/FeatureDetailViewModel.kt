/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.feature_detail

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtConfigCommands
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.blue_sdk.features.extended.hs_datalog_config.HSDataLogConfig
import com.st.blue_sdk.features.extended.hs_datalog_config.request.HSDCmd
import com.st.blue_sdk.features.extended.hs_datalog_config.request.HSDataLogCommand
import com.st.blue_sdk.logger.CsvFileLogger
import com.st.blue_sdk.logger.LogCatLogger
import com.st.blue_sdk.logger.db_logger.DbLogger
import com.st.blue_sdk.services.calibration.CalibrationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeatureDetailViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService
) : ViewModel() {

    companion object {
        private val TAG = FeatureDetailViewModel::class.simpleName
    }

    val featureUpdates: State<FeatureUpdate<*>?>
        get() = _featureUpdates

    private val _featureUpdates = mutableStateOf<FeatureUpdate<*>?>(null)

    fun startCalibration(deviceId: String, featureName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blueManager.nodeFeatures(deviceId)
                .find { it.name == featureName && it.name == Compass.NAME }
                ?.let { feature ->
                    val isCalibrated =
                        calibrationService.startCalibration(nodeId = deviceId, feature = feature)

                    if (!isCalibrated.status) {
                        blueManager.getConfigControlUpdates(nodeId = deviceId).collect {
                            if (it is CalibrationStatus) {
                                Log.d(TAG, "calibration status ${it.status}")
                            }
                        }
                    }
                }
        }
    }

    private var observeFeatureJob: Job? = null
    fun observeFeature(featureName: String, deviceId: String) {
        observeFeatureJob?.cancel()
        blueManager.nodeFeatures(deviceId).find { it.name == featureName }?.let { feature ->
            observeFeatureJob = blueManager.getFeatureUpdates(nodeId = deviceId, features = listOf(feature))
                .flowOn(Dispatchers.IO)
                .onEach {
                    _featureUpdates.value = it
                }.launchIn(viewModelScope)
        }
    }

    fun sendExtendedCommand(featureName: String, deviceId: String) {

        viewModelScope.launch {

            val feature =
                blueManager.nodeFeatures(deviceId).find { it.name == featureName } ?: return@launch

            if (feature is ExtConfiguration) {
                val command = ExtConfigCommands.buildConfigCommand(ExtConfigCommands.BANKS_STATUS)
                val response =
                    blueManager.writeFeatureCommand(
                        deviceId,
                        ExtendedFeatureCommand(feature, command)
                    )
                response?.let {
                    Log.d("TAG", response.toString())
                    it.commandId
                }
            }

            if (feature is HSDataLogConfig) {
                val commands = listOf(
                    HSDCmd.buildHSDGetCmdDevice(),
                    HSDCmd.buildHSDGetCmdTagConfig(),
                )
                commands.forEach {
                    val response =
                        blueManager.writeFeatureCommand(
                            deviceId,
                            HSDataLogCommand(feature = feature, cmd = it)
                        )
                    response?.let {
                        it.commandId
                    }

                    delay(1000)
                }
            }
        }
    }

    fun disconnectFeature(deviceId: String, featureName: String) {
        observeFeatureJob?.cancel()
        _featureUpdates.value = null
        viewModelScope.launch {
            val features = blueManager.nodeFeatures(deviceId).filter { it.name == featureName }
            blueManager.disableFeatures(
                nodeId = deviceId,
                features = features
            )
        }
    }

    fun isLogCatLoggerEnabled(deviceId: String): Boolean =
        blueManager.getAllLoggers(deviceId)
            .find { it.id == LogCatLogger.TAG && it.isEnabled } != null

    fun isCsvLoggerEnabled(deviceId: String): Boolean =
        blueManager.getAllLoggers(deviceId)
            .find { it.id == CsvFileLogger.TAG && it.isEnabled } != null

    fun isDbLoggerEnabled(deviceId: String): Boolean =
        blueManager.getAllLoggers(deviceId)
            .find { it.id == DbLogger.TAG && it.isEnabled } != null

    fun enableDisableLogCatLogger(deviceId: String, newState: Boolean) {
        if (newState) {
            blueManager.enableAllLoggers(nodeId = deviceId, loggerTags = listOf(LogCatLogger.TAG))
        } else {
            blueManager.disableAllLoggers(nodeId = deviceId, loggerTags = listOf(LogCatLogger.TAG))
        }
    }

    fun enableDisableCsvLogger(deviceId: String, newState: Boolean) {
        if (newState) {
            blueManager.enableAllLoggers(nodeId = deviceId, loggerTags = listOf(CsvFileLogger.TAG))
        } else {
            blueManager.disableAllLoggers(nodeId = deviceId, loggerTags = listOf(CsvFileLogger.TAG))
        }
    }

    fun enableDisableDbLogger(deviceId: String, newState: Boolean) {
        if (newState) {
            blueManager.enableAllLoggers(nodeId = deviceId, loggerTags = listOf(DbLogger.TAG))
        } else {
            blueManager.disableAllLoggers(nodeId = deviceId, loggerTags = listOf(DbLogger.TAG))
        }
    }
}