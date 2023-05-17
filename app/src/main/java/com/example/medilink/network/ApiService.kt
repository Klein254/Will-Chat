package com.example.medilink.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface ApiService {
    @POST("send")
    fun sendmessage(
        @HeaderMap headers: HashMap<String?, String?>?,
        @Body messageBody: String?
    ): Call<String?>?
}