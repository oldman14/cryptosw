package jp.co.tokairika.cryptogw.api

import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CryptoInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val httpUrl = chain.request().url
        val requestBuilder = chain.request().newBuilder().url(httpUrl)
            .addHeader(KEY_CONTENT_TYPE, VAL_CONTENT_TYPE)
            .addHeader(KEY_TIMEZONE, VAL_TIMEZONE)
        try {
            return chain.proceed(requestBuilder.build())
        } catch (exception: UnknownHostException) {
            throw UnknownHostException(httpUrl.encodedPath)
        } catch (exception: ConnectException) {
            throw ConnectException(httpUrl.encodedPath)
        } catch (exception: SocketTimeoutException) {
                throw SocketTimeoutException(httpUrl.encodedPath)
        } catch (exception: Exception) {
            throw exception
        }

    }

    companion object {
        private const val KEY_CONTENT_TYPE: String = "Content-Type"
        private const val VAL_CONTENT_TYPE: String = "application/json; charset=UTF-8"
        private const val KEY_TIMEZONE: String = "Timezone"
        private const val VAL_TIMEZONE: String = "Asia/Tokyo"
    }
}