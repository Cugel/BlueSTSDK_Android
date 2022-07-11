/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.bt.advertise.AdvertiseFilter
import com.st.blue_sdk.bt.advertise.BlueNRGAdvertiseFilter
import com.st.blue_sdk.bt.advertise.BlueSTSDKAdvertiseFilter
import com.st.blue_sdk.bt.advertise.getFwInfo
import com.st.blue_sdk.common.Resource
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.exported.AudioOpusConfFeature
import com.st.blue_sdk.features.exported.AudioOpusMusicFeature
import com.st.blue_sdk.features.exported.AudioOpusVoiceFeature
import com.st.blue_sdk.features.exported.ExportedFeature
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.services.NodeServerConsumer
import com.st.blue_sdk.services.NodeServerProducer
import com.st.blue_sdk.services.NodeServiceConsumer
import com.st.blue_sdk.services.NodeServiceProducer
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.blue_sdk.services.ota.FwConsole
import com.st.blue_sdk.services.ota.OtaService
import com.st.blue_sdk.services.ota.UpgradeStrategy
import com.st.blue_sdk.utils.hasBluetoothPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BlueManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: BluetoothManager,
    private val catalog: BoardCatalogRepo,
    private val nodeServiceConsumer: NodeServiceConsumer,
    private val nodeServiceProducer: NodeServiceProducer,
    private val nodeServerConsumer: NodeServerConsumer,
    private val nodeServerProducer: NodeServerProducer,
    private val otaService: OtaService
) : BlueManager {

    companion object {
        private val TAG = BlueManager::class.java.simpleName
        private const val EXPORTED_SERVICE = "00000000-0001-11e1-9ab4-0002a5d5c51b"
        private val EXPORTED_MAP: MutableMap<UUID, List<ExportedFeature>> = mutableMapOf()
    }

    init {
        EXPORTED_MAP[UUID.fromString(EXPORTED_SERVICE)] = listOf(
            AudioOpusVoiceFeature(),
            AudioOpusConfFeature(),
            AudioOpusMusicFeature()
        )
    }

    private val filters: List<AdvertiseFilter> = listOf(
        BlueSTSDKAdvertiseFilter(),
        BlueNRGAdvertiseFilter()
    )
    private var stopDeviceScan = false
    private val scanPeriod = 10000L

    override fun getAllLoggers(nodeId: String): List<Logger> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getAllLoggers()
    }

    override fun anyFeatures(nodeId: String, features: List<String>): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getNodeFeatures().any {
            features.contains(it.name)
        }
    }

    override fun allFeatures(nodeId: String, features: List<String>): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getNodeFeatures().filter {
            features.contains(it.name)
        }.distinctBy { it.name }.size == features.distinct().size
    }

    override fun addAllLoggers(loggers: List<Logger>) {
        nodeServiceConsumer.getNodeServices().forEach { it.addLoggers(loggers) }
    }

    override fun disableAllLoggers(nodeId: String?, loggerTags: List<String>) {
        if (nodeId == null) {
            nodeServiceConsumer.getNodeServices().forEach { it.disableAllLoggers(loggerTags) }
        } else {
            nodeServiceConsumer.getNodeService(nodeId)?.let { service ->
                service.disableAllLoggers(loggerTags)
            }
        }
    }

    override fun enableAllLoggers(nodeId: String?, loggerTags: List<String>) {
        if (nodeId == null) {
            nodeServiceConsumer.getNodeServices().forEach { it.enableAllLoggers(loggerTags) }
        } else {
            nodeServiceConsumer.getNodeService(nodeId)?.let { service ->
                service.enableAllLoggers(loggerTags)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun scanNodes(): Flow<Resource<List<Node>>> =
        callbackFlow {

            if (context.hasBluetoothPermission().not()) {
                throw IllegalStateException("Missing BlueTooth Permissions")
            }

            stopDeviceScan = false
            nodeServiceProducer.clear()

            val bleScanner = bleManager.adapter.bluetoothLeScanner

            trySend(Resource.loading())

            val scanCallback = object : ScanCallback() {
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    bleScanner.stopScan(this)
                    trySend(
                        Resource.error(
                            R.string.blue_st_sdk_error_ble_scan_failed,
                            errorCode,
                            null
                        )
                    )
                    close()
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result?.let {
                        if (addScannedDeviceToCollection(filters, it)) {
                            trySend(
                                Resource.loading(
                                    nodeServiceConsumer.getNodeServices()
                                        .map { service -> service.getNode() })
                            )
                        }
                    }
                }
            }

            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

            bleScanner.startScan(listOf(), scanSettings, scanCallback)

            launch {
                withTimeoutOrNull(scanPeriod) {
                    while (stopDeviceScan.not()) {
                        delay(200)
                    }
                }

                trySend(
                    Resource.success(
                        nodeServiceConsumer.getNodeServices().map { service -> service.getNode() })
                )
                close()
            }

            awaitClose {
                bleScanner.stopScan(scanCallback)
            }

        }.flowOn(Dispatchers.IO)
            .catch { e ->
                Log.e(TAG, "scan exception", e)
                emit(Resource.error(R.string.blue_st_sdk_error_ble_scan_failed, data = null))
            }

    override fun stopScan() {
        stopDeviceScan = true
    }

    @Synchronized
    private fun addScannedDeviceToCollection(
        filters: List<AdvertiseFilter>,
        scanResult: ScanResult
    ): Boolean {

        val advertisingData = scanResult.scanRecord ?: return false

        if (filters.isNotEmpty()) {
            val advertiseInfo =
                filters.asSequence()
                    .map { it.decodeAdvertiseData(advertisingData.bytes) }
                    .filter { advInfo -> advInfo != null }
                    .firstOrNull() ?: return false

            if (hasService(deviceAddress = scanResult.device.address).not()) {
                nodeServiceProducer.createService(
                    scanResult = scanResult,
                    advertiseInfo = advertiseInfo
                )
                return true
            }
        }

        return false
    }

    private fun hasService(deviceAddress: String) =
        nodeServiceConsumer.getNodeService(deviceAddress) != null

    @SuppressLint("MissingPermission")
    override fun connectToNode(nodeId: String): Flow<Node> {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: throw IllegalStateException(
            "Unable to find NodeService for $nodeId"
        )

        connectFromNode(node = nodeService.bleHal.getDevice())

        stopScan()
        return nodeService.connectToNode(autoConnect = false)
    }


    override fun getNode(nodeId: String) =
        nodeServiceConsumer.getNodeService(nodeId)?.getNode()

    override fun getNodeStatus(nodeId: String) =
        (nodeServiceConsumer.getNodeService(nodeId)?.getDeviceStatus()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId"))

    override fun isConnected(nodeId: String): Boolean =
        nodeServiceConsumer.getNodeService(nodeId)?.isConnected()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

    override fun isReady(nodeId: String): Boolean =
        nodeServiceConsumer.getNodeService(nodeId)?.isReady()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

    @SuppressLint("MissingPermission")
    override fun disconnect(nodeId: String?) {
        if (nodeId.isNullOrEmpty()) {
            nodeServiceConsumer.getNodeServices().forEach { it.disconnect() }
            nodeServiceProducer.clear()
            disconnectFromNode()
        } else {
            disconnectFromNode(nodeId = nodeId)
            val service = nodeServiceConsumer.getNodeService(nodeId)
            service?.disconnect()
            nodeServiceProducer.removeService(nodeId)
        }
    }

    override fun nodeFeatures(nodeId: String): List<Feature<*>> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getNodeFeatures()
    }

    override suspend fun enableFeatures(
        nodeId: String,
        features: List<Feature<*>>
    ): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.setFeaturesNotifications(features = features, true)
    }

    override suspend fun disableFeatures(
        nodeId: String,
        features: List<Feature<*>>
    ): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.setFeaturesNotifications(features = features, false)
    }

    override fun getFeatureUpdates(
        nodeId: String,
        features: List<Feature<*>>
    ): Flow<FeatureUpdate<*>> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getFeatureUpdates(features)
    }

    override suspend fun writeDebugMessage(nodeId: String, msg: String): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.writeDebugMessage(msg)
    }

    override fun getConfigControlUpdates(nodeId: String): Flow<FeatureResponse> {

        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getConfigControlUpdates()
    }

    override fun getDebugMessages(nodeId: String): Flow<DebugMessage>? {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")
        return service.getDebugMessages()
    }

    override suspend fun writeFeatureCommand(
        nodeId: String,
        featureCommand: FeatureCommand,
        responseTimeout: Long,
        retry: Int,
        retryDelay: Long
    ): FeatureResponse? {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.writeFeatureCommand(
            featureCommand = featureCommand,
            responseTimeout = responseTimeout,
            retry = retry,
            retryDelay = retryDelay
        )
    }

    override suspend fun setBoardCatalog(
        fileUri: Uri,
        contentResolver: ContentResolver
    ): List<BoardFirmware> =
        catalog.setBoardCatalog(fileUri, contentResolver)

    override suspend fun getBoardFirmware(nodeId: String): BoardFirmware? {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return null
        val advInfo = nodeService.getNode().advertiseInfo ?: return null
        return advInfo.getFwInfo()?.let { catalog.getFwDetailsNode(it.deviceId, it.fwId) }
    }

    override suspend fun upgradeFw(nodeId: String): FwConsole? {
        return otaService.updateFirmware(nodeId)
    }

    override fun getFwUpdateStrategy(nodeId: String): UpgradeStrategy {
        return otaService.getFwUpdateStrategy(nodeId)
    }

    private fun connectFromNode(node: Node): Boolean {
        val server = nodeServerConsumer.getNodeServer(node.device.address)
            ?: nodeServerProducer.createServer(node, EXPORTED_MAP)
        return server.connectToPeripheral()
    }

    private fun disconnectFromNode(nodeId: String? = null): Boolean {
        if (nodeId == null) {
            nodeServerConsumer.getNodeServers().forEach {
                it.disconnectFromPeripheral()
            }

            nodeServerProducer.clear()

            return true
        } else {
            nodeServerConsumer.getNodeServer(nodeId)?.let { server ->
                server.disconnectFromPeripheral()

                nodeServerProducer.removeServer(nodeId = nodeId)

                return true
            }
        }

        return false
    }
}
