package top.checka.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // CHANGE THIS TO YOUR ACTUAL DOMAIN
    private const val BASE_URL = "https://admin.checka.top/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: CheckaApiService by lazy {
        retrofit.create(CheckaApiService::class.java)
    }
}
