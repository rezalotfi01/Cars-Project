package com.reza.carsproject.di

import com.reza.carsproject.di.modules.dataModule
import com.reza.carsproject.di.modules.repositoryModule
import com.reza.carsproject.di.modules.viewModelModule

val appComponents = listOf(
     dataModule
    , viewModelModule
    , repositoryModule
)