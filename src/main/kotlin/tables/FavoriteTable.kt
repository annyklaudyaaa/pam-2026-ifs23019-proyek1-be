package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FavoriteTable : UUIDTable("favorites") {
    // Relasi ke User
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)

    // Relasi ke Artist
    val artistId = uuid("artist_id").references(ArtistTable.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = timestamp("created_at")

    // Constraint: User tidak boleh mem-favoritkan artis yang sama lebih dari satu kali
    init {
        uniqueIndex("user_artist_unique", userId, artistId)
    }
}