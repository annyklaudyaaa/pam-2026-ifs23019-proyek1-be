package org.delcom.repositories

import org.delcom.dao.AlbumDAO
import org.delcom.entities.Album
import org.delcom.helpers.albumDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.AlbumTable
import org.jetbrains.exposed.sql.*
import java.util.*

class AlbumRepository : IAlbumRepository {
    override suspend fun getByArtistId(artistId: String): List<Album> = suspendTransaction {
        val artistUUID = UUID.fromString(artistId)
        AlbumDAO.find { AlbumTable.artistId eq artistUUID }
            .orderBy(AlbumTable.releaseDate to SortOrder.DESC)
            .map(::albumDAOToModel)
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

    override suspend fun delete(albumId: String): Boolean = suspendTransaction {
        val rowsDeleted = AlbumTable.deleteWhere { AlbumTable.id eq UUID.fromString(albumId) }
        rowsDeleted >= 1
    }
}