package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AlbumTable : UUIDTable("albums") {
    // Relasi ke Artis
    val artistId = uuid("artist_id").references(ArtistTable.id, onDelete = ReferenceOption.CASCADE)

    // Judul Album
    val title = varchar("title", 100)

    // Tanggal Rilis
    val releaseDate = varchar("release_date", 20)

    // URL Cover
    val coverUrl = text("cover_url").nullable()

    // Tipe Album (Penyebab error 500 tadi)
    val type = varchar("type", 30).default("Full Album")

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}