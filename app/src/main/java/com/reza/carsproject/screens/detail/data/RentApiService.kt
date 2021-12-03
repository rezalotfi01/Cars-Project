package com.reza.carsproject.screens.detail.data

import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RentApiService {
    companion object{
        private const val BASE_URL = "https://4i96gtjfia.execute-api.eu-central-1.amazonaws.com"

        fun createRetrofit(okClient: OkHttpClient) = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okClient)
            .build()
            .create(RentApiService::class.java)
    }


    @POST("/default/wunderfleet-recruiting-mobile-dev-quick-rental")
    fun rentCar(@Body body: RentRequest): Single<RentDetail>
}