package org.delcom.dao

import org.delcom.tables.ArtistTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ArtistDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArtistDAO>(ArtistTable)

    // Foreign Key ke User yang mengelola data ini
    var userId by ArtistTable.userId

    // Informasi Artis/Grup SM Entertainment
    var name by ArtistTable.name
    var category by ArtistTable.category
    var description by ArtistTable.description
    var imageUrl by ArtistTable.imageUrl
    var debutYear by ArtistTable.debutYear
    var status by ArtistTable.status

    // Timestamps
    var createdAt by ArtistTable.createdAt
    var updatedAt by ArtistTable.updatedAt
}