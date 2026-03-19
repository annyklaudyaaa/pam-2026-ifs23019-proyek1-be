package org.delcom.entities

import kotlinx.serialization.Serializable

@Serializable
data class ArtistWithAlbums(
    val artist: Artist,
    val albums: List<Album>
)