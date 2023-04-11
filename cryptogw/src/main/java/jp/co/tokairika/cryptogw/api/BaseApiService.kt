package jp.co.tokairika.cryptogw.api


import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.security.GwKeyStore
import retrofit2.HttpException
import retrofit2.Response

internal abstract class BaseApiService {
    protected val service
        get() = CryptoGWApiProvider.getCryptoGWApiService()

    private val sdkToken: String
        get() {
            val tokenEncrypt = GwPreferenceUtils.sdkToken
            val tokenDecrypt = GwKeyStore().decrypt(tokenEncrypt, GwKeyStore.Alias.SDK_TOKEN)
            return tokenDecrypt.toString(Charsets.UTF_8)
        }

    /**
     * Call api with sdk token
     * If result not successful and status code == 401, refresh sdk token and call api one more time
     */
    protected suspend fun <T> getNetworkResultWithHeaderToken(
        retryTimes: Int = 0,
        call: suspend (String) -> Response<T>,
    ): T? {
        if (GwPreferenceUtils.sdkToken.isEmpty()) {
            refreshNewToken()
        }
        val authorization = CryptoGWApiProvider.TOKEN_PREFIX + sdkToken
        val result = call.invoke(authorization)
        if (!result.isSuccessful) {
            val httpException = HttpException(result)
            if (httpException.code() == 401 && retryTimes < RETRY_TIMES) {
                refreshNewToken()
                return getNetworkResultWithHeaderToken(retryTimes + 1, call)
            }
            throw httpException
        }
        return result.body()
    }

    /**
     * Get new sdk token and save it in preference
     */
    suspend fun refreshNewToken() {
        val token = CryptoGWApiProvider.refreshSdkToken()
        GwPreferenceUtils.sdkToken =
            GwKeyStore().encrypt(token.toByteArray(), GwKeyStore.Alias.SDK_TOKEN)
    }

    private companion object {
        /**
         * Retry time call refresh sdk token and call api if token expires
         */
        const val RETRY_TIMES: Int = 2
    }
}
