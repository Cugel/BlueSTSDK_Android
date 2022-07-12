@file:UseSerializers(BoardFotaTypeSerializer::class,BootLoaderTypeSerializer::class)

package com.st.blue_sdk.board_catalog.models

import com.st.blue_sdk.board_catalog.api.serializers.BoardFotaTypeSerializer
import com.st.blue_sdk.board_catalog.api.serializers.BootLoaderTypeSerializer
import kotlinx.serialization.UseSerializers



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FotaDetails(
    @SerialName("partial_fota")
    val partial_fota: Int?=0,
    @SerialName("type")
    val type: BoardFotaType?=BoardFotaType.NO,
    @SerialName("max_chunk_length")
    var max_chunk_length: Int?=0,
    @SerialName("max_divisor_constraint")
    var max_divisor_constraint: Int?=0,
    @SerialName("fw_url")
    val fw_url: String?=null,
    @SerialName("bootloader_type")
    val bootloader_type: BootLoaderType?=BootLoaderType.NONE,
)
