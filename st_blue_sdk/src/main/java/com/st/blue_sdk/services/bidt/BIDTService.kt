package com.st.blue_sdk.services.bidt

import com.st.blue_sdk.services.debug.DebugMessage
import kotlinx.coroutines.flow.Flow

interface BIDTService {

    suspend fun init()

    fun hasBIDTService(): Boolean

    suspend fun write(data: ByteArray, mtu: Int? = null): Int

    fun getReadData(): Flow<ByteArray>

    suspend fun read(timeout: Long = 1000): ByteArray?

    fun getMaxPayloadSize(): Int
}