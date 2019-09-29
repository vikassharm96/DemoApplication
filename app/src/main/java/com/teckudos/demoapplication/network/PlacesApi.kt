package com.teckudos.demoapplication.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/"

enum class PlacesApiFilter(val value: String) {
    SHOW_HOTEL("hotel"), SHOW_SCHOOL("school"), SHOW_HOSPITAL(
        "hospital"
    ),
    SHOW_ALL("all")
}

private val moshi = Moshi.Builder().build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface PlacesApiService {
    @GET("json?")
    fun getPlaces(
        @Query("location") loc: String,
        @Query("radius") radius: String,
        @Query("type") type: String,
        @Query("key") key: String
    ): Deferred<PlacesDTO>
}

object PlacesApi {
    val retrofitService: PlacesApiService by lazy {
        retrofit.create(PlacesApiService::class.java)
    }
}