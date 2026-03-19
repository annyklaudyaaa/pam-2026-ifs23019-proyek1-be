package org.delcom.dao

import org.delcom.tables.AlbumTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class AlbumDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AlbumDAO>(AlbumTable)

    // Foreign Key: Merujuk ke ID Artist (Pemilik Album)
    var artistId by AlbumTable.artistId

    // Informasi Album
    var title by AlbumTable.title
    var releaseDate by AlbumTable.releaseDate
    var coverUrl by AlbumTable.coverUrl
    var type by AlbumTable.type

    // Timestamps
    var createdAt by AlbumTable.createdAt
    var updatedAt by AlbumTable.updatedAt
}