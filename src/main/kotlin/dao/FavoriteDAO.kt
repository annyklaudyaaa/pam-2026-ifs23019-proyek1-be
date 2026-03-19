package org.delcom.dao

import org.delcom.tables.FavoriteTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class FavoriteDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FavoriteDAO>(FavoriteTable)

    var userId by FavoriteTable.userId
    var artistId by FavoriteTable.artistId
    var createdAt by FavoriteTable.createdAt
}