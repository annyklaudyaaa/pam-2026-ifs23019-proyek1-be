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

    // Informasi Utama
    var name: String,         // Contoh: "aespa", "NCT Dream"
    var category: String,     // Contoh: "Boy Group", "Girl Group", "Soloist"
    var description: String,
    var imageUrl: String?,    // Path foto artis

    // Field tambahan sesuai topik Entertainment
    var debutYear: Int = 2000,
    var status: String = "Active", // "Active", "Hiatus", "Disbanded"

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)