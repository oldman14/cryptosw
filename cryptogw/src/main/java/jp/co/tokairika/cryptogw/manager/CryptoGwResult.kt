package jp.co.tokairika.cryptogw.manager

import jp.co.tokairika.cryptogw.manager.exception.DkCommandFailedException
import jp.co.tokairika.cryptogw.manager.exception.LockDeviceConnectFailedException
import jp.co.tokairika.cryptogw.utils.takeIfEmpty
import jp.co.tokairika.cryptogw.utils.toHexString
import jp.co.tokairika.cryptosdk.DkbException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * DK関連処理結果クラス
 */
open class CryptoGwResult internal constructor(val resultType: ResultType, val message: String) {
    private class MessageBuilder(private val resultType: ResultType) {
        fun build(exception: Exception): String {
            return when (exception) {
                is HttpException -> build(httpException = exception)
                is DkbException -> build(dkbException = exception)
                is LockDeviceConnectFailedException -> build(lockDeviceConnectFailedException = exception)
                is DkCommandFailedException -> build(dkCommandFailedException = exception)
                is UnknownHostException, is ConnectException, is SocketTimeoutException -> resultType.detail(exception.message)
                else -> {
                    if (resultType == ResultType.UNEXPECTED_ERROR) {
                        return resultType.detail(exception.toString())
                    }

                    return resultType.detail()
                }

            }
        }

        fun build(httpException: HttpException): String {
//            if (resultType == ResultType.UNEXPECTED_ERROR) {
            val endPoint = httpException.response()?.raw()?.request?.url?.encodedPath.takeIfEmpty()
            return resultType.detail(endPoint)
//            }

//            return resultType.detail()
        }

        fun build(dkbException: DkbException): String {
            return resultType.detail("${dkbException.errorMessage} (${dkbException.errorCode})")
        }

        fun build(lockDeviceConnectFailedException: LockDeviceConnectFailedException): String {
            return build(lockDeviceConnectFailedException.dkbException) + lockDeviceConnectFailedException.info?.toHexString()
        }

        fun build(dkCommandFailedException: DkCommandFailedException): String {
            val exception = dkCommandFailedException.cause as? Exception
            if (exception != null) {
                return build(exception)
            }
            return resultType.detail(dkCommandFailedException.toString())
        }
    }

    companion object {
        internal fun build(exception: Exception): CryptoGwResult {
            val resultType = ResultType.from(exception)
            val message = MessageBuilder(resultType).build(exception)
            return CryptoGwResult(resultType, message)
        }
    }

    val isSucceeded: Boolean
        get() = resultType == ResultType.SUCCESS

    internal constructor(resultType: ResultType) : this(resultType, resultType.detail())
}