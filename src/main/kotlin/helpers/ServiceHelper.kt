package org.delcom.helpers

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.data.AppException
import org.delcom.entities.User
import org.delcom.repositories.IUserRepository

object ServiceHelper {

    /**
     * Mengambil User yang sedang login berdasarkan JWT Token.
     */
    suspend fun getAuthUser(call: ApplicationCall, userRepository: IUserRepository): User {
        val principal = call.principal<JWTPrincipal>()
            ?: throw AppException(401, "Sesi tidak valid, silakan login kembali")

        val userId = principal.payload.getClaim("id").asString()
            ?: throw AppException(401, "Token tidak dikenali")

        val user = userRepository.getById(userId)
            ?: throw AppException(401, "User tidak ditemukan atau sudah tidak aktif")

        return user
    }

    /**
     * Helper untuk validasi request.
     * Sekarang bisa mengakses 'validator.errors' karena aksesnya sudah dibuka.
     */
    fun validateRequest(validator: ValidatorHelper) {
        if (validator.errors.isNotEmpty()) {
            throw AppException(400, "Input tidak valid", validator.errors.joinToString("|"))
        }
    }
}