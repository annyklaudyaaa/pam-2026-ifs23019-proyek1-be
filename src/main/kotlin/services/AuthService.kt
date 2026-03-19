package org.delcom.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.AppException
import org.delcom.data.AuthRequest
import org.delcom.data.DataResponse
import org.delcom.data.RefreshTokenRequest
import org.delcom.entities.RefreshToken
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.hashPassword
import org.delcom.helpers.verifyPassword
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
) {
    // 1. Register
    suspend fun postRegister(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        val existUser = userRepository.getByUsername(request.username)
        if (existUser != null) {
            // FIX: Hapus errors.joinToString karena ini bukan error validasi input
            throw AppException(409, "Akun dengan username ini sudah terdaftar!")
        }

        request.password = hashPassword(request.password)
        val userId = userRepository.create(request.toEntity())

        call.respond(DataResponse("success", "Berhasil pendaftaran", mapOf("userId" to userId)))
    }

    // 2. Login
    suspend fun postLogin(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("username", "Username wajib diisi")
        validator.required("password", "Password wajib diisi")
        validator.validate()

        val existUser = userRepository.getByUsername(request.username)
            ?: throw AppException(404, "Kredensial tidak valid!")

        val validPassword = verifyPassword(request.password, existUser.password)
        if (!validPassword) {
            throw AppException(404, "Kredensial tidak valid!")
        }

        // Buat Token
        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("id", existUser.id) // Pakai "id" agar sinkron dengan ServiceHelper
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        refreshTokenRepository.deleteByUserId(existUser.id)

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(
                userId = existUser.id,
                authToken = authToken,
                refreshToken = strRefreshToken
            )
        )

        call.respond(DataResponse("success", "Login Berhasil", mapOf(
            "authToken" to authToken,
            "refreshToken" to strRefreshToken
        )))
    }

    // 3. Refresh Token
    suspend fun postRefreshToken(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("refreshToken", "Refresh Token wajib ada")
        validator.required("authToken", "Auth Token wajib ada")
        validator.validate()

        val existRefreshToken = refreshTokenRepository.getByToken(request.refreshToken, request.authToken)
        refreshTokenRepository.delete(request.authToken)

        if(existRefreshToken == null) {
            throw AppException(401, "Sesi tidak valid!")
        }

        val user = userRepository.getById(existRefreshToken.userId)
            ?: throw AppException(404, "User tidak ditemukan!")

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("id", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(userId = user.id, authToken = authToken, refreshToken = strRefreshToken)
        )

        call.respond(DataResponse("success", "Token diperbarui", mapOf(
            "authToken" to authToken,
            "refreshToken" to strRefreshToken
        )))
    }

    // 4. Logout
    suspend fun postLogout(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("authToken", "Token wajib ada")
        validator.validate()

        try {
            val decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(request.authToken)
            val userId = decodedJWT.getClaim("id").asString()

            if (userId != null) {
                refreshTokenRepository.deleteByUserId(userId)
            }
            refreshTokenRepository.delete(request.authToken)

            call.respond(DataResponse("success", "Berhasil logout", null))
        } catch (e: Exception) {
            throw AppException(401, "Token sudah tidak valid")
        }
    }
}