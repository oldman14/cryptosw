package jp.co.tokairika.cryptogw.manager.exception

import jp.co.tokairika.cryptogw.manager.ResultType

@Suppress("unused")
internal enum class DkError(
    val prefixErrorCode: String,
    val suffixErrorCode: String,
    val resultType: ResultType
) {
    // DKエラー定義 記載無しのコードはUNEXPECTED_ERRORとする
    DK_ERROR_CODE_0101("1", "0101", ResultType.UNINITIALIZED),
    DK_ERROR_CODE_0106("1", "0106", ResultType.INVALID_DIGITAL_KEY),
    DK_ERROR_CODE_0107("1", "0107", ResultType.INVALID_DIGITAL_KEY),
    DK_ERROR_CODE_0108("1", "0108", ResultType.INVALID_DIGITAL_KEY),
    DK_ERROR_CODE_0109("1", "0109", ResultType.INVALID_DIGITAL_KEY),
    DK_ERROR_CODE_010A("1", "010A", ResultType.INVALID_DIGITAL_KEY),
    DK_ERROR_CODE_0301("1", "0301", ResultType.BLE_SETTING_REQUIRE),
    DK_ERROR_CODE_0302("1", "0302", ResultType.BLE_PERMISSION_REQUIRE),
    DK_ERROR_CODE_0303("1", "0303", ResultType.LOCATION_SETTING_REQUIRE),
    DK_ERROR_CODE_0304("1", "0304", ResultType.LOCATION_PERMISSION_REQUIRE),
    DK_ERROR_CODE_0305("1", "0305", ResultType.BLE_CONNECTION_FAILED),
    DK_ERROR_CODE_0308("1", "0308", ResultType.BLE_CONNECTION_FAILED),
    DK_ERROR_CODE_0309("1", "0309", ResultType.BLE_CONNECTION_FAILED),
    DK_ERROR_CODE_0310("1", "0310", ResultType.BLE_CONNECTION_TIMEOUT),
    DK_ERROR_CODE_030A("1", "030A", ResultType.BLE_NOT_SUPPORT),
    DK_ERROR_CODE_030B("1", "030B", ResultType.BLE_NOT_SUPPORT),
    DK_ERROR_CODE_030C("1", "030C", ResultType.BLE_CONNECTION_FAILED),
    DK_ERROR_CODE_030D("1", "030D", ResultType.BLE_COMMUNICATION_FAILED),
    DK_ERROR_CODE_030E("1", "030E", ResultType.BLE_COMMUNICATION_FAILED),
    DK_ERROR_CODE_030F("1", "030F", ResultType.BLE_WRITE_COMMAND_INVALID);

    companion object {
        /**
         * エラーコードからエラー種別を取得する
         *
         * @param code エラーコード
         * @return エラー種別
         */
        fun from(code: String): ResultType {
            return if (code.take( 1) == "1") {
                // 先頭が"1"で末尾が一致するものは定義に従ったエラー種別を返却する
                values().find { suffixErrorCode ->
                    suffixErrorCode.suffixErrorCode == code.takeLast(
                        4
                    )
                }?.resultType ?: ResultType.UNEXPECTED_ERROR
            } else {
                // 先頭が"1"以外のものは想定外エラーに丸め込む
                ResultType.UNEXPECTED_ERROR
            }
        }
    }
}