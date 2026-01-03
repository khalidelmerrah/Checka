package top.checka.app.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://admin.checka.top/api/"
    
    // Session token storage - set after successful authentication
    var sessionToken: String? = null

    /**
     * Authorization interceptor - adds Bearer token to all requests
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Skip auth header for auth.php endpoint
        if (originalRequest.url.encodedPath.contains("auth.php")) {
            return@Interceptor chain.proceed(originalRequest)
        }
        
        // Add Authorization header for all other requests
        val token = sessionToken
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(newRequest)
    }

    private val retrofit: Retrofit by lazy {
        // Logging interceptor for debugging
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        // PRODUCTION-READY CLIENT - Uses standard SSL certificate validation
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add auth header
            .addInterceptor(logging) // Add logging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: CheckaApiService by lazy {
        retrofit.create(CheckaApiService::class.java)
    }

    /**
     * Clear session token (for logout)
     */
    fun clearSession() {
        sessionToken = null
    }
}
