package jp.co.tokairika.cryptogw.manager

import jp.co.tokairika.cryptogw.manager.exception.*
import jp.co.tokairika.cryptosdk.DkbException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 返却用の処理結果定義
 *
 * @property code コード
 * @property message メッセージ
 */
enum class ResultType(internal val code: Int, private val message: String) {
    SUCCESS(0, ""),
    UNAUTHORIZED(1, "認証エラーが発生しました"),
    TIMEOUT_ERROR(2, "サーバーとの通信がタイムアウトしました"),
    NETWORK_ERROR(3, "ネットワークエラーが発生しました"),
    SERVER_ERROR(4, "サーバーエラーが発生しました"),
    UNEXPECTED_ERROR(5, "予期せぬエラーが発生しました"),

    // TODO オフライン利用許可チェックはSDK内で行わないようになったためエラーコード一覧整理後修正
    NOT_ALLOW_OFFLINE(6, "オフライン利用が許可されていません"),
    INVALID_DIGITAL_KEY(7, "無効なデジタルキーです"),
    NOT_EXIST_DIGITAL_KEY(8, "デジタルキーを発行してください"),
    COMMAND_FAILED(9, "コマンドの実行に失敗しました"),
    UNINITIALIZED(10, "初期化処理が未実施です"),
    BLE_SETTING_REQUIRE(11, "端末のBLUETOOTH設定をONにしてください"),
    BLE_PERMISSION_REQUIRE(12, "アプリのBLUETOOTHパーミションを許可してください"),
    LOCATION_SETTING_REQUIRE(13, "端末の位置情報設定をONにしてください"),
    LOCATION_PERMISSION_REQUIRE(14, "アプリの位置情報パーミションを許可してください"),
    BLE_CONNECTION_FAILED(15, "BLUETOOTH接続に失敗しました"),
    BLE_NOT_SUPPORT(16, "BLUETOOTHが利用できません"),
    BLE_COMMUNICATION_FAILED(17, "BLUETOOTH通信に失敗しました"),
    OUT_OF_TERM_DIGITAL_KEY(18, "デジタルキー利用可能期間外です"),
    BLE_CONNECTION_TIMEOUT(19, "BLUETOOTH通信にタイムアウトしました"),
    ISSUABLE_DIGITAL_KEYS_LIMIT_ERROR(20, "デジタルキー発行上限数によりダウンロード対象なし"),
    BLE_WRITE_COMMAND_INVALID(21, "BLEコマンド不正");

    private class ResultTypeMapper {
        fun map(exception: Exception): ResultType {
            return when (exception) {
                is HttpException -> map(httpException = exception)
                is UnknownHostException, is ConnectException -> NETWORK_ERROR
                is SocketTimeoutException -> TIMEOUT_ERROR
                is DkbException -> map(dkbException = exception)
                is DkCommandFailedException -> map(dkCommandFailedException = exception)
                is CryptoGwNotInitializedException -> UNINITIALIZED
                is InvalidDigitalKeyException -> INVALID_DIGITAL_KEY
                is LockDeviceConnectFailedException -> map(lockDeviceConnectFailedException = exception)
                is NotExistDigitalKeyException -> NOT_EXIST_DIGITAL_KEY
                is OutOfTermDigitalKeyException -> OUT_OF_TERM_DIGITAL_KEY
                is IssuableDigitalKeysLimitException -> ISSUABLE_DIGITAL_KEYS_LIMIT_ERROR
                else -> UNEXPECTED_ERROR
            }
        }

        private fun map(httpException: HttpException): ResultType {
            return when (httpException.code()) {
                401 -> UNAUTHORIZED
                500 -> SERVER_ERROR
                else -> UNEXPECTED_ERROR
            }
        }

        fun map(dkbException: DkbException): ResultType {
            return DkError.from(dkbException.errorCode)
        }

        fun map(dkCommandFailedException: DkCommandFailedException): ResultType {
            val exception = dkCommandFailedException.cause as? Exception ?: return COMMAND_FAILED
            return map(exception = exception)

        }

        fun map(lockDeviceConnectFailedException: LockDeviceConnectFailedException): ResultType {
            val info = lockDeviceConnectFailedException.info
                ?: return map(dkbException = lockDeviceConnectFailedException.dkbException)

            val dkbExceptionInfo = DkbExceptionInfo(info)
            return when (dkbExceptionInfo.cryptoGwErrorType) {
                DkbErrorNotification.CryptoGwErrorType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                DkbErrorNotification.CryptoGwErrorType.INVALID_DIGITAL_KEY -> INVALID_DIGITAL_KEY
                DkbErrorNotification.CryptoGwErrorType.OUT_OF_TERM_DIGITAL_KEY -> OUT_OF_TERM_DIGITAL_KEY
                DkbErrorNotification.CryptoGwErrorType.COMMAND_FAILED -> COMMAND_FAILED
                DkbErrorNotification.CryptoGwErrorType.NOT_ERROR -> UNEXPECTED_ERROR
            }
        }
    }

    /**
     * 詳細メッセージを付与する
     *
     * @param addMessage 付け加えるメッセージ
     * @return 詳細メッセージ
     */
    internal fun detail(addMessage: String? = null): String {
        return if (addMessage == null) {
            this.message
        } else {
            this.message + MESSAGE_DETAIL + addMessage
        }
    }

    internal companion object {
        private const val MESSAGE_DETAIL = "\n 例外："

        internal fun from(exception: Exception): ResultType {
            return ResultTypeMapper().map(exception)
        }
    }
}