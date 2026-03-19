package org.delcom.repositories

import org.delcom.dao.AlbumDAO
import org.delcom.entities.Album
import org.delcom.helpers.albumDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.AlbumTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq // PENTING: Fix Unresolved 'eq'
import java.util.*

class AlbumRepository : IAlbumRepository {

    override suspend fun getByArtistId(artistId: String): List<Album> = suspendTransaction {
        val artistUUID = UUID.fromString(artistId)
        AlbumDAO.find { AlbumTable.artistId eq artistUUID }
            .orderBy(AlbumTable.releaseDate to SortOrder.DESC)
            .map(::albumDAOToModel)
    }

    // FIX: Implementasi getById (Wajib karena ada di Interface)
    override suspend fun getById(albumId: String): Album? = suspendTransaction {
        AlbumDAO.findById(UUID.fromString(albumId))?.let(::albumDAOToModel)
    }

    override suspend fun create(album: Album): String = suspendTransaction {
        val albumDAO = AlbumDAO.new {
            artistId = UUID.fromString(album.artistId)
            title = album.title
            releaseDate = album.releaseDate
            coverUrl = album.coverUrl
            type = album.type
            createdAt = album.createdAt
            updatedAt = album.updatedAt
        }
        albumDAO.id.value.toString()
    }

    // FIX: Implementasi update (Wajib karena ada di Interface)
    override suspend fun update(albumId: String, newAlbum: Album): Boolean = suspendTransaction {
        val albumDAO = AlbumDAO.findById(UUID.fromString(albumId)) ?: return@suspendTransaction false
        albumDAO.title = newAlbum.title
        albumDAO.releaseDate = newAlbum.releaseDate
        albumDAO.coverUrl = newAlbum.coverUrl
        albumDAO.type = newAlbum.type
        albumDAO.updatedAt = newAlbum.updatedAt
        true
    }

    override suspend fun delete(albumId: String): Boolean = suspendTransaction {
        val rowsDeleted = AlbumTable.deleteWhere { AlbumTable.id eq UUID.fromString(albumId) }
        rowsDeleted >= 1
    }
}