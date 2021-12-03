package com.reza.carsproject.di.modules

import com.reza.carsproject.screens.detail.data.DetailApiService
import com.reza.carsproject.screens.detail.data.RentApiService
import com.reza.carsproject.screens.maps.data.MapsApiService
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

private const val NAME_OKHTTP_WITH_HEADER = "okhttp_with_header"
private const val NAME_OKHTTP = "okhttp_normal"

val dataModule = module {

    // Provide OkHTTPClient
    single(qualifier = named(NAME_OKHTTP)) {
        OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    // Provide OkHTTPClient with header
    single(qualifier = named(NAME_OKHTTP_WITH_HEADER)){
        OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer df7c313b47b7ef87c64c0f5f5cebd6086bbb0fa")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    // Provide MapsApiService
    single {
        MapsApiService.createRetrofit(get(qualifier = named(NAME_OKHTTP)))
    }

    // Provide DetailApiService
    single {
        DetailApiService.createRetrofit(get(qualifier = named(NAME_OKHTTP)))
    }

    // Provide RentApiService
    single {
        RentApiService.createRetrofit(get(qualifier = named(NAME_OKHTTP_WITH_HEADER)))
    }
}