package org.delcom.repositories

import org.delcom.dao.ArtistDAO
import org.delcom.dao.AlbumDAO // Tambahkan import ini
import org.delcom.entities.Artist
import org.delcom.entities.ArtistWithAlbums // Tambahkan import ini
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.artistDAOToModel
import org.delcom.helpers.albumDAOToModel // Tambahkan import ini
import org.delcom.tables.ArtistTable
import org.delcom.tables.AlbumTable // Tambahkan import ini
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class ArtistRepository : IArtistRepository {

    // 1. Mengambil semua data dengan Pagination, Search, dan Filter
    override suspend fun getAll(
        userId: String,
        search: String,
        page: Int,
        perPage: Int,
        category: String?,
        status: String?
    ): List<Artist> = suspendTransaction {
        val userUUID = UUID.fromString(userId)
        val offsetValue = (page - 1).toLong() * perPage

        val query = ArtistTable.selectAll().where { ArtistTable.userId eq userUUID }

        if (search.isNotBlank()) {
            val keyword = "%${search.lowercase()}%"
            query.andWhere { ArtistTable.name.lowerCase() like keyword }
        }

        if (!category.isNullOrBlank()) {
            query.andWhere { ArtistTable.category eq category }
        }

        if (!status.isNullOrBlank()) {
            query.andWhere { ArtistTable.status eq status }
        }

        ArtistDAO.wrapRows(query)
            .orderBy(ArtistTable.createdAt to SortOrder.DESC)
            .limit(perPage)
            .offset(offsetValue)
            .map(::artistDAOToModel)
    }

    // 2. Mengambil Statistik untuk Dashboard
    override suspend fun getStats(userId: String): Map<String, Long> = suspendTransaction {
        val userUUID = UUID.fromString(userId)
        val total = ArtistTable.selectAll().where { ArtistTable.userId eq userUUID }.count()
        val girlGroups = ArtistTable.selectAll().where {
            (ArtistTable.userId eq userUUID) and (ArtistTable.category eq "Girl Group")
        }.count()
        val boyGroups = ArtistTable.selectAll().where {
            (ArtistTable.userId eq userUUID) and (ArtistTable.category eq "Boy Group")
        }.count()

        mapOf(
            "total" to total,
            "girlGroups" to girlGroups,
            "boyGroups" to boyGroups,
            "others" to (total - girlGroups - boyGroups)
        )
    }

    override suspend fun getById(artistId: String): Artist? = suspendTransaction {
        ArtistDAO.findById(UUID.fromString(artistId))?.let(::artistDAOToModel)
    }

    // --- FITUR UNGGULAN: Interactive Discography ---
    override suspend fun getByIdWithAlbums(artistId: String): ArtistWithAlbums? = suspendTransaction {
        val artistUUID = UUID.fromString(artistId)
        val artistDao = ArtistDAO.findById(artistUUID) ?: return@suspendTransaction null

        // Ambil semua album milik artis ini dari AlbumTable
        val albums = AlbumDAO.find { AlbumTable.artistId eq artistUUID }
            .orderBy(AlbumTable.releaseDate to SortOrder.DESC)
            .map(::albumDAOToModel)

        ArtistWithAlbums(
            artist = artistDAOToModel(artistDao),
            albums = albums
        )
    }

    override suspend fun create(artist: Artist): String = suspendTransaction {
        val artistDAO = ArtistDAO.new {
            userId = UUID.fromString(artist.userId)
            name = artist.name
            category = artist.category
            description = artist.description
            imageUrl = artist.imageUrl
            debutYear = artist.debutYear
            status = artist.status
            createdAt = artist.createdAt
            updatedAt = artist.updatedAt
        }
        artistDAO.id.value.toString()
    }

    override suspend fun update(userId: String, artistId: String, newArtist: Artist): Boolean = suspendTransaction {
        val artistDAO = ArtistDAO
            .find {
                (ArtistTable.id eq UUID.fromString(artistId)) and
                        (ArtistTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (artistDAO != null) {
            artistDAO.name = newArtist.name
            artistDAO.category = newArtist.category
            artistDAO.description = newArtist.description
            artistDAO.imageUrl = newArtist.imageUrl
            artistDAO.debutYear = newArtist.debutYear
            artistDAO.status = newArtist.status
            artistDAO.updatedAt = newArtist.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun delete(userId: String, artistId: String): Boolean = suspendTransaction {
        val rowsDeleted = ArtistTable.deleteWhere {
            (ArtistTable.id eq UUID.fromString(artistId)) and
                    (ArtistTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }
}