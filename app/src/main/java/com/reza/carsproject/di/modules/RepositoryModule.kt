package com.reza.carsproject.di.modules

import com.reza.carsproject.screens.detail.data.DetailRepository
import com.reza.carsproject.screens.maps.data.MapsRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Provides Maps Repository
    single {
        MapsRepository(get())
    }

    // Provides Detail Repository
    single {
        DetailRepository(get(),get())
    }
}