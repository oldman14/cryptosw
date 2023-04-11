package jp.co.tokairika.cryptogw.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jp.co.tokairika.cryptogw.Environment
import jp.co.tokairika.cryptogw.manager.exception.CryptoGwNotInitializedException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

internal object CryptoGWApiProvider {

    private const val API_TIMEOUT: Long = 3
    const val TOKEN_PREFIX = "Bearer "

    private lateinit var retrofit: Retrofit
    private lateinit var service: CryptoGWApi
    private lateinit var tokenRefreshHandler: TokenRefreshHandler

    /**
     *  initialize service retrofit to call mobile api
     *  @param tokenRefreshHandler to get new sdk token
     */
    fun init(tokenRefreshHandler: TokenRefreshHandler) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl(Environment.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(getClient())
            .build()
        service = retrofit.create(CryptoGWApi::class.java)
        this.tokenRefreshHandler = tokenRefreshHandler
    }

    /**
     *  @return CryptoGWApi service
     */
    fun getCryptoGWApiService(): CryptoGWApi {
        if (!this::service.isInitialized) {
            throw CryptoGwNotInitializedException()
        }
        return service
    }

    /**
     *  get new sdk token
     */
    suspend fun refreshSdkToken(): String {
        if (!this::tokenRefreshHandler.isInitialized) {
            throw CryptoGwNotInitializedException()
        }
        return tokenRefreshHandler.refreshTokenAsync()
    }

    /**
     *  get OkHttpClient
     *  @see API_TIMEOUT read timeout
     *  timeunit  = SECONDS
     */
    private fun getClient(): OkHttpClient {
        val logInterceptor = HttpLoggingInterceptor()
        val cryptoInterceptor = CryptoInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .readTimeout(API_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(API_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(API_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(cryptoInterceptor)
            .addInterceptor(logInterceptor)
            .build()
    }
}