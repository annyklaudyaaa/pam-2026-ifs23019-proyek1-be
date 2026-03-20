package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Artist // Pastikan kamu juga mengubah Todo menjadi Artist di package entities
import java.util.UUID

@Serializable
data class ArtistRequest(
    var userId: String = "",
    var name: String = "",         // Nama Idol (misal: Mark Lee)
    var groupName: String = "",    // Nama Grup (misal: NCT 127)
    var photoUrl: String? = null,  // Link foto
    var position: String = "Member", // Posisi (Main Vocal, Dancer, dll)
    var isActive: Boolean = true,    // Status aktif di SM
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "groupName" to groupName,
            "photoUrl" to photoUrl,
            "position" to position,
            "isActive" to isActive,
        )
    }

    fun toEntity(): Artist {
        return Artist(
            // Pastikan data class Artist (Entity) kamu juga sudah disesuaikan field-nya
            userId = userId,
            name = name,
            groupName = groupName,
            photoUrl = photoUrl,
            position = position,
            isActive = isActive,
            updatedAt = Clock.System.now()
        )
    }
}