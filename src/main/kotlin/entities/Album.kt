package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Album(
    var id: String = UUID.randomUUID().toString(),
    var artistId: String, // Menghubungkan ke ID Artist

    // Informasi Album
    var title: String,        // Contoh: "Savage", "Hot Sauce"
    var releaseDate: String,  // Contoh: "2024-10-21"
    var coverUrl: String?,    // Path foto cover album

    // Bumbu tambahan untuk filter/kategori album
    var type: String = "Full Album", // Contoh: "Mini Album", "Digital Single"

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)