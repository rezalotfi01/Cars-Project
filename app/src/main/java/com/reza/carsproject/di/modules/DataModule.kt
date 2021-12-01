package com.reza.carsproject.di.modules

import com.reza.carsproject.screens.maps.data.MapsApiService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val dataModule = module {

    // Provide OkHTTPClient
    single {
        OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    // Provide MapsApiService
    single {
        MapsApiService.createRetrofit(get())
    }


}