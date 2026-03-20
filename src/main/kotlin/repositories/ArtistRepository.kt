package org.delcom.repositories

import org.delcom.dao.ArtistDAO
import org.delcom.entities.Artist
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.artistDAOToModel // Gunakan helper yang sudah kita ubah tadi
import org.delcom.tables.ArtistTable // Pastikan merujuk ke ArtistTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class ArtistRepository : IArtistRepository {

    // 1. Mengambil data dengan Pagination, Search (Nama), dan Filter (Grup/Status)
    override suspend fun getAll(
        userId: String,
        search: String,
        page: Int,
        perPage: Int,
        isActive: Boolean?, // Ganti isDone
        groupName: String?  // Ganti urgency menjadi filter grup
    ): List<Artist> = suspendTransaction {
        val userUUID = UUID.fromString(userId)
        val offsetValue = (page - 1).toLong() * perPage

        // Query dasar berdasarkan User ID (admin yang menginput)
        val query = ArtistTable.selectAll().where { ArtistTable.userId eq userUUID }

        // Fitur Search: Berdasarkan Nama Artis (Case Insensitive)
        if (search.isNotBlank()) {
            val keyword = "%${search.lowercase()}%"
            query.andWhere { ArtistTable.name.lowerCase() like keyword }
        }

        // Fitur Filter: Status Aktif
        if (isActive != null) {
            query.andWhere { ArtistTable.isActive eq isActive }
        }

        // Fitur Filter: Berdasarkan Nama Grup (NCT, aespa, dll)
        if (!groupName.isNullOrBlank()) {
            query.andWhere { ArtistTable.groupName eq groupName }
        }

        // Eksekusi dengan Pagination dan Sorting (Terbaru dulu)
        ArtistDAO.wrapRows(query)
            .orderBy(ArtistTable.createdAt to SortOrder.DESC)
            .limit(perPage).offset(start = offsetValue)
            .map(::artistDAOToModel)
    }

    // 2. Mengambil Statistik Artis untuk Dashboard SM Entertainment
    override suspend fun getStats(userId: String): Map<String, Long> = suspendTransaction {
        val userUUID = UUID.fromString(userId)

        val total = ArtistTable.selectAll().where { ArtistTable.userId eq userUUID }.count()
        val activeCount = ArtistTable.selectAll().where {
            (ArtistTable.userId eq userUUID) and (ArtistTable.isActive eq true)
        }.count()
        val inactiveCount = total - activeCount

        mapOf(
            "total_artists" to total,
            "active_artists" to activeCount,
            "inactive_artists" to inactiveCount
        )
    }

    override suspend fun getById(artistId: String): Artist? = suspendTransaction {
        ArtistDAO.findById(UUID.fromString(artistId))?.let(::artistDAOToModel)
    }

    override suspend fun create(artist: Artist): String = suspendTransaction {
        val artistDAO = ArtistDAO.new {
            userId = UUID.fromString(artist.userId)
            name = artist.name
            groupName = artist.groupName
            photoUrl = artist.photoUrl
            position = artist.position
            isActive = artist.isActive
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
            artistDAO.groupName = newArtist.groupName
            artistDAO.photoUrl = newArtist.photoUrl
            artistDAO.position = newArtist.position
            artistDAO.isActive = newArtist.isActive
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