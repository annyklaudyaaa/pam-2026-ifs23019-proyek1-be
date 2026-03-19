package org.delcom.repositories

import org.delcom.entities.Album

interface IAlbumRepository {
    // Mengambil semua album dari artis tertentu (untuk Discography)
    suspend fun getByArtistId(artistId: String): List<Album>

    // CRUD Standar untuk Album
    suspend fun getById(albumId: String): Album?
    suspend fun create(album: Album): String
    suspend fun update(albumId: String, newAlbum: Album): Boolean
    suspend fun delete(albumId: String): Boolean
}