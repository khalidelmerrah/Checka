package top.checka.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // CHANGE THIS TO YOUR ACTUAL DOMAIN
    private const val BASE_URL = "https://admin.checka.top/api/"

    private val retrofit: Retrofit by lazy {
        val logging = okhttp3.logging.HttpLoggingInterceptor()
        logging.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY)

        // Unsafe Client to bypass SSL errors (DEBUG ONLY)
        val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : javax.net.ssl.X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })
        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        val client = okhttp3.OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
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
}
