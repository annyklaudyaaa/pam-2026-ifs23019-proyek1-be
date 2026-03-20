package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Artist(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var name: String,           // Nama Idol/Artis
    var groupName: String,      // Nama Grup (misal: aespa, NCT)
    var isActive: Boolean = true, // Status aktif di agensi
    var photoUrl: String?,       // Foto profil
    var position: String = "Member", // Posisi dalam grup

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)