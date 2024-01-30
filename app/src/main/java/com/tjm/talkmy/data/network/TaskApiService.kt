package com.tjm.talkmy.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path

interface TaskApiService {
    @GET("webPage/{url}")
    suspend fun getFromUrl(@Path("url") url:String):Response<String>
}