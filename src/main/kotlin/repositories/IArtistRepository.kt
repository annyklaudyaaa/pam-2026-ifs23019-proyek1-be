package org.delcom.repositories

import org.delcom.entities.Artist

interface IArtistRepository {
    // Mengambil daftar artis dengan Search dan Filter (Category & Status)
    suspend fun getAll(
        userId: String,
        search: String,
        page: Int,
        perPage: Int,
        category: String?,
        status: String?
    ): List<Artist>

    // Statistik untuk Dashboard (Contoh: Jumlah Boy Group vs Girl Group)
    suspend fun getStats(userId: String): Map<String, Long>

    // Operasi CRUD Standar
    suspend fun getById(artistId: String): Artist?
    suspend fun create(artist: Artist): String
    suspend fun update(userId: String, artistId: String, newArtist: Artist): Boolean
    suspend fun delete(userId: String, artistId: String): Boolean
}