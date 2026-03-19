package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.ArtistService
import org.delcom.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {
    // User Repository & Service
    single<IUserRepository> {
        UserRepository()
    }

    single {
        UserService(get(), get())
    }

    // Auth & Token Management
    single<IRefreshTokenRepository> {
        RefreshTokenRepository()
    }

    single {
        AuthService(jwtSecret, get(), get())
    }

    // Artist Repository (SM Entertainment Topic)
    single<IArtistRepository> {
        ArtistRepository()
    }

    // Artist Service
    single {
        ArtistService(get(), get())
    }
}