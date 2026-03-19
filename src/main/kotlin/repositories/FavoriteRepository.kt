package org.delcom.repositories

import org.delcom.dao.ArtistDAO
import org.delcom.entities.Artist
import org.delcom.helpers.artistDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.FavoriteTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class FavoriteRepository : IFavoriteRepository {

    // Fitur Bias System: Menambah/Menghapus dari daftar favorit
    override suspend fun toggleFavorite(userId: String, artistId: String): Boolean = suspendTransaction {
        val userUUID = UUID.fromString(userId)
        val artistUUID = UUID.fromString(artistId)

        val existing = FavoriteTable.selectAll().where {
            (FavoriteTable.userId eq userUUID) and (FavoriteTable.artistId eq artistUUID)
        }.singleOrNull()

        if (existing == null) {
            FavoriteTable.insert {
                it[FavoriteTable.userId] = userUUID
                it[FavoriteTable.artistId] = artistUUID
            }
            true // Berhasil difavoritkan
        } else {
            FavoriteTable.deleteWhere {
                (FavoriteTable.userId eq userUUID) and (FavoriteTable.artistId eq artistUUID)
            }
            false // Berhasil dihapus dari favorit
        }
    }

    // Mengambil semua "Bias" (Artis Favorit) milik User
    override suspend fun getFavorites(userId: String): List<Artist> = suspendTransaction {
        val userUUID = UUID.fromString(userId)

        // Join antara tabel Favorite dan Artist untuk mengambil data lengkap artisnya
        (FavoriteTable innerJoin ArtistTable)
            .selectAll()
            .where { FavoriteTable.userId eq userUUID }
            .map { row ->
                // Menggunakan wrapRow untuk mengubah Row hasil Join menjadi ArtistDAO, lalu ke Model
                artistDAOToModel(ArtistDAO.wrapRow(row))
            }
    }
}