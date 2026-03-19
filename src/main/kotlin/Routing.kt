package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.services.ArtistService
import org.delcom.services.AuthService
import org.delcom.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val artistService: ArtistService by inject()
    val authService: AuthService by inject()
    val userService: UserService by inject()

    install(StatusPages) {
        // Menangani error spesifik dari aplikasi kita (Validator, Database, dll)
        exception<AppException> { call, cause ->
            // FIX: Gunakan cause.statusCode (sesuaikan dengan AppException.kt)
            val status = HttpStatusCode.fromValue(cause.statusCode)

            call.respond(
                status = status,
                message = ErrorResponse(
                    status = "fail",
                    message = cause.message,
                    // cause.data berisi string error dipisah '|' dari ValidatorHelper
                    data = cause.data
                )
            )
        }

        // Menangani error umum/sistem
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Terjadi kesalahan internal pada server",
                    data = null
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("SM Entertainment API is running. Welcome!")
        }

        // --- AUTH ROUTES ---
        route("/auth") {
            post("/login") { authService.postLogin(call) }
            post("/register") { authService.postRegister(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout") { authService.postLogout(call) }
        }

        // --- PROTECTED ROUTES ---
        authenticate(JWTConstants.NAME) {
            route("/users") {
                get("/me") { userService.getMe(call) }
                put("/me") { userService.putMe(call) }
                put("/me/password") { userService.putMyPassword(call) }
                put("/me/photo") { userService.putMyPhoto(call) }
            }

            route("/artists") {
                get { artistService.getAll(call) }
                get("/stats") { artistService.getStats(call) }
                post { artistService.post(call) }
                get("/{id}") { artistService.getById(call) }
                put("/{id}") { artistService.put(call) }
                put("/{id}/image") { artistService.putImage(call) }
                delete("/{id}") { artistService.delete(call) }

                // Tambahkan endpoint untuk Bias System jika belum ada
                post("/{id}/favorite") { artistService.toggleFavorite(call) }
                get("/favorites") { artistService.getMyFavorites(call) }
            }
        }

        route("/images") {
            get("/users/{id}") { userService.getPhoto(call) }
            get("/artists/{id}") { artistService.getImage(call) }
        }
    }
}