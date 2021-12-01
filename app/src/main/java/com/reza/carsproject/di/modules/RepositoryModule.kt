package com.reza.carsproject.di.modules

import com.reza.carsproject.screens.maps.data.MapsRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        MapsRepository(get())
    }
}