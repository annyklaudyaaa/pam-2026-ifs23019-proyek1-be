package org.delcom.repositories

import org.delcom.entities.Artist

interface IArtistRepository {
    suspend fun getAll(
        userId: String,
        search: String,
        page: Int,
        perPage: Int,
        isActive: Boolean?, // Ganti dari isDone
        groupName: String?  // Ganti dari urgency
    ): List<Artist>

    suspend fun getStats(userId: String): Map<String, Long>

    suspend fun getById(artistId: String): Artist? // Ganti todoId menjadi artistId

    suspend fun create(artist: Artist): String

    suspend fun update(
        userId: String,
        artistId: String,
        newArtist: Artist
    ): Boolean // Ganti todoId menjadi artistId

    suspend fun delete(
        userId: String,
        artistId: String
    ): Boolean // Ganti todoId menjadi artistId
}