/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog

import android.content.ContentResolver
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.st.blue_sdk.board_catalog.api.BoardCatalogApi
import com.st.blue_sdk.board_catalog.db.BoardCatalogDao
import com.st.blue_sdk.board_catalog.di.Preferences
import com.st.blue_sdk.board_catalog.models.BoardCatalog
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoardCatalogRepoImpl @Inject constructor(
    private val api: BoardCatalogApi,
    private val db: BoardCatalogDao,
    private val json: Json,
    @Preferences private val pref: SharedPreferences
) : BoardCatalogRepo {

    private var cache: MutableList<BoardFirmware> = mutableListOf()

    private suspend fun needSync(): Boolean {
        if (cache.isEmpty()) return true

        val now = Date().time
        val lastSyncTimestamp = pref.getLong(LAST_SYNC_TIMESTAMP_PREF, 0L)
        val lastSyncDate = Date(lastSyncTimestamp)
        val minSyncInterval = Date(now - MIN_SYNC_INTERVAL)

        return if (lastSyncDate.before(minSyncInterval)) {
            val remoteChecksum = api.getDBVersion().checksum
            val localChecksum = pref.getString(LAST_SYNC_CHECKSUM_PREF, "")
            val isEqual = remoteChecksum == localChecksum

            pref.edit(commit = true) { putString(LAST_SYNC_CHECKSUM_PREF, remoteChecksum) }

            return isEqual
        } else {
            false
        }
    }

    private suspend fun sync() {
        db.deleteAllEntries()
        cache.clear()
        val firmwares = api.getFirmwares()
        firmwares.bleListBoardFirmwareV1?.let {
            cache.addAll(it)
            db.add(it)
        }
        firmwares.bleListBoardFirmwareV2?.let {
            cache.addAll(it)
            db.add(it)
        }

        val remoteChecksum = api.getDBVersion().checksum
        pref.edit(commit = true) { putLong(LAST_SYNC_TIMESTAMP_PREF, Date().time) }
        pref.edit(commit = true) { putString(LAST_SYNC_CHECKSUM_PREF, remoteChecksum) }
    }

    override suspend fun reset() {
        sync()
    }

    override suspend fun getBoardCatalog(): List<BoardFirmware> {
        withContext(Dispatchers.IO) {
            if (cache.isEmpty()) {
                cache.addAll(db.getDeviceFirmwares())
            }

            if (needSync()) {
                sync()
            }
        }

        return cache
    }

    override suspend fun getFwDetailsNode(deviceId: String, bleFwId: String): BoardFirmware? {
        if (needSync()) {
            sync()
        }
        return cache.find { it.bleFwId == bleFwId && deviceId == it.bleDevId }
    }

    override suspend fun setBoardCatalog(
        fileUri: Uri,
        contentResolver: ContentResolver
    ): List<BoardFirmware> {
        kotlin.runCatching {
            contentResolver.openInputStream(fileUri)
        }.getOrNull()?.bufferedReader(StandardCharsets.ISO_8859_1)?.readText()?.let {
            json.decodeFromString<BoardCatalog>(it)
        }?.let { boardCatalog ->
            db.deleteAllEntries()
            cache.clear()
            boardCatalog.bleListBoardFirmwareV1?.let {
                db.add(it)
            }
            boardCatalog.bleListBoardFirmwareV2?.let {
                db.add(it)
            }
            cache.addAll(db.getDeviceFirmwares())
        }

        return cache
    }

    companion object {
        const val LAST_SYNC_TIMESTAMP_PREF = "LAST_SYNC_TIMESTAMP_PREF"
        const val LAST_SYNC_CHECKSUM_PREF = "LAST_SYNC_CHECKSUM_PREF"
        const val MIN_SYNC_INTERVAL = 24 * 60 * 60 * 1000L
    }
}
