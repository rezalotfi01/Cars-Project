package com.reza.carsproject.screens.maps.data

import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * All REST API endpoints that needed in Maps Screen implemented here
 */
interface MapsApiService {
    companion object {
        private const val BASE_URL = "https://s3.eu-central-1.amazonaws.com/"

        fun createRetrofit(okClient: OkHttpClient) = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okClient)
            .build()
            .create(MapsApiService::class.java)
    }


    @GET("/wunderfleet-recruiting-dev/cars.json")
    fun getAllCars(): Single<List<Car>>

}