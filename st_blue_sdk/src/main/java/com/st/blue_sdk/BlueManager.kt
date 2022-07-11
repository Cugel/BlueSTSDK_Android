/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk

import android.content.ContentResolver
import android.net.Uri
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.common.Resource
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.blue_sdk.services.ota.FwConsole
import com.st.blue_sdk.services.ota.UpgradeStrategy
import kotlinx.coroutines.flow.Flow

interface BlueManager {

    fun anyFeatures(nodeId: String, features: List<String>): Boolean

    fun allFeatures(nodeId: String, features: List<String>): Boolean

    fun addAllLoggers(loggers: List<Logger>)

    fun getAllLoggers(nodeId: String): List<Logger>

    fun disableAllLoggers(nodeId: String? = null, loggerTags: List<String>)

    fun enableAllLoggers(nodeId: String? = null, loggerTags: List<String>)

    suspend fun scanNodes(): Flow<Resource<List<Node>>>

    fun stopScan()

    fun connectToNode(nodeId: String): Flow<Node>

    fun getNode(nodeId: String): Node?

    fun getNodeStatus(nodeId: String): Flow<Node>

    fun isConnected(nodeId: String): Boolean

    fun isReady(nodeId: String): Boolean

    fun disconnect(nodeId: String? = null)

    fun nodeFeatures(nodeId: String): List<Feature<*>>

    suspend fun enableFeatures(nodeId: String, features: List<Feature<*>>): Boolean

    suspend fun disableFeatures(nodeId: String, features: List<Feature<*>>): Boolean

    fun getFeatureUpdates(nodeId: String, features: List<Feature<*>>): Flow<FeatureUpdate<*>>

    suspend fun writeDebugMessage(nodeId: String, msg: String): Boolean

    fun getConfigControlUpdates(nodeId: String): Flow<FeatureResponse>

    fun getDebugMessages(nodeId: String): Flow<DebugMessage>?

    suspend fun writeFeatureCommand(
        nodeId: String,
        featureCommand: FeatureCommand,
        responseTimeout: Long = 500L,
        retry: Int = 0,
        retryDelay: Long = 200
    ): FeatureResponse?

    suspend fun setBoardCatalog(fileUri: Uri, contentResolver: ContentResolver): List<BoardFirmware>

    suspend fun getBoardFirmware(nodeId: String): BoardFirmware?

    suspend fun upgradeFw(nodeId: String): FwConsole?

    fun getFwUpdateStrategy(nodeId: String): UpgradeStrategy
}
