/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog

import android.content.ContentResolver
import android.net.Uri
import com.st.blue_sdk.board_catalog.models.BoardFirmware

interface BoardCatalogRepo {
    suspend fun reset()

    suspend fun getBoardCatalog(): List<BoardFirmware>

    suspend fun getFwDetailsNode(deviceId: String, bleFwId: String): BoardFirmware?

    suspend fun setBoardCatalog(fileUri: Uri, contentResolver: ContentResolver): List<BoardFirmware>
}
