package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
class AppException(
    val statusCode: Int,          // Tambahkan 'val' di sini
    override val message: String, // 'override val' sudah benar karena ini properti
    val data: String? = null      // Tambahkan 'val' di sini
) : Exception(message)