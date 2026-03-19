package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Artist
import java.util.UUID

@Serializable
data class ArtistRequest(
    var userId: String = "",
    var name: String = "",
    var category: String = "", // Boy Group, Girl Group, Soloist
    var description: String = "",
    var imageUrl: String? = null,
    var debutYear: Int = 2000,
    var status: String = "Active",
) {
    // Digunakan oleh ValidatorHelper untuk mengecek input kosong
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl,
            "debutYear" to debutYear,
            "status" to status,
        )
    }

    // Mengubah Request menjadi Entity Artist untuk diproses oleh Repository
    fun toEntity(): Artist {
        return Artist(
            // id tidak perlu diisi karena sudah ada default UUID di Entity
            userId = userId,
            name = name,
            category = category,
            description = description,
            imageUrl = imageUrl,
            debutYear = debutYear,
            status = status,
            createdAt = Clock.System.now(), // HAPUS .toString() agar tetap bertipe Instant
            updatedAt = Clock.System.now()  // HAPUS .toString() agar tetap bertipe Instant
        )
    }
}