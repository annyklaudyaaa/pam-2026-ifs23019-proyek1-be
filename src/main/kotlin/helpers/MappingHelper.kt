package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.*
import org.delcom.entities.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Helper untuk menjalankan query database di Dispatcher IO.
 * Penting agar operasi database tidak memblokir thread utama server.
 */
suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

/** * MAPPERS: Mengubah objek DAO (Database) menjadi Model (Entity)
 */

// 1. Mapper untuk User
fun userDAOToModel(dao: UserDAO) = User(
    id = dao.id.value.toString(),
    name = dao.name,
    username = dao.username,
    password = dao.password,
    photo = dao.photo,
    about = dao.about,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

// 2. Mapper untuk Refresh Token
fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken = dao.authToken,
    createdAt = dao.createdAt,
)

// 3. Mapper untuk Artist (SM Entertainment)
fun artistDAOToModel(dao: ArtistDAO) = Artist(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    name = dao.name,
    category = dao.category,
    description = dao.description,
    imageUrl = dao.imageUrl,
    debutYear = dao.debutYear,
    status = dao.status,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

// 4. Mapper untuk Album (Interactive Discography)
fun albumDAOToModel(dao: AlbumDAO) = Album(
    id = dao.id.value.toString(),
    artistId = dao.artistId.toString(),
    title = dao.title,
    releaseDate = dao.releaseDate,
    coverUrl = dao.coverUrl,
    type = dao.type,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

// 5. Mapper untuk Favorite (Bias System)
fun favoriteDAOToModel(dao: FavoriteDAO) = Favorite(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    artistId = dao.artistId.toString(),
    createdAt = dao.createdAt
)