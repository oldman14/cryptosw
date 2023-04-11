package jp.co.tokairika.cryptogw.manager.exception

/**
 * Dkbエラー通知の定数定義群
 */
internal class DkbErrorNotification {

    /**
     * Dkbエラー通知を丸め込んだ結果種別
     */
    enum class CryptoGwErrorType {
        UNEXPECTED_ERROR,
        INVALID_DIGITAL_KEY,
        OUT_OF_TERM_DIGITAL_KEY,
        COMMAND_FAILED,
        NOT_ERROR;

        companion object {
            fun from(
                dkbErrorType: DkbErrorType?,
                dkbErrorInformation: DkbErrorInformation?
            ): CryptoGwErrorType {
                if (dkbErrorType == null || dkbErrorInformation == null) return UNEXPECTED_ERROR

                when (dkbErrorType) {
                    DkbErrorType.AUTH -> {
                        return when (dkbErrorInformation) {
                            DkbErrorInformation.FORMAT_OR_SEQUENCE_ERROR,
                            DkbErrorInformation.TIMEOUT -> {
                                COMMAND_FAILED
                            }
                            else -> {
                                UNEXPECTED_ERROR
                            }
                        }
                    }
                    DkbErrorType.ONE_TIME_KEY_AUTH -> {
                        return when (dkbErrorInformation) {
                            DkbErrorInformation.FORMAT_OR_SEQUENCE_ERROR,
                            DkbErrorInformation.TIMEOUT -> {
                                COMMAND_FAILED
                            }
                            DkbErrorInformation.OUTDATED -> {
                                OUT_OF_TERM_DIGITAL_KEY
                            }
                            DkbErrorInformation.ROLLING_COUNT,
                            DkbErrorInformation.ILLEGAL_USER,
                            DkbErrorInformation.FDS_WRITE_FAILED -> {
                                INVALID_DIGITAL_KEY
                            }
                            else -> {
                                UNEXPECTED_ERROR
                            }
                        }
                    }
                    DkbErrorType.USER_AUTH -> {
                        return when (dkbErrorInformation) {
                            DkbErrorInformation.FORMAT_OR_SEQUENCE_ERROR,
                            DkbErrorInformation.TIMEOUT,
                            DkbErrorInformation.FDS_WRITE_FAILED -> {
                                COMMAND_FAILED
                            }
                            DkbErrorInformation.OUTDATED -> {
                                OUT_OF_TERM_DIGITAL_KEY
                            }
                            DkbErrorInformation.ILLEGAL_USER,
                            DkbErrorInformation.RETURN_CODE_ERROR -> {
                                INVALID_DIGITAL_KEY
                            }
                            else -> {
                                UNEXPECTED_ERROR
                            }
                        }
                    }
                    DkbErrorType.DKB_CONTROL_REQUEST -> {
                        return when (dkbErrorInformation) {
                            DkbErrorInformation.FORMAT_OR_SEQUENCE_ERROR,
                            DkbErrorInformation.TIMEOUT -> {
                                COMMAND_FAILED
                            }
                            else -> {
                                UNEXPECTED_ERROR
                            }
                        }
                    }
                    DkbErrorType.NOTIFY_TIME_CONTROL -> {
                        return when (dkbErrorInformation) {
                            DkbErrorInformation.SERVER_CONNECTION_ERROR,
                            DkbErrorInformation.TIMEOUT -> {
                                NOT_ERROR
                            }
                            else -> {
                                UNEXPECTED_ERROR
                            }
                        }
                    }
                    DkbErrorType.LOG_NOTIFICATION_CONTROL -> {
                        return NOT_ERROR
                    }
                    else -> {
                        return UNEXPECTED_ERROR
                    }
                }

            }
        }
    }

    /**
     * 共通エラー エラー種別
     */
    enum class DkbErrorType(val errorType: Int) {
        AUTH(0x00),
        ONE_TIME_KEY_AUTH(0x01),
        USER_AUTH(0x02),
        DKB_CONTROL_REQUEST(0x03),
        NOTIFY_TIME_CONTROL(0x04),
        PUT_BACK_CONTROL(0x05),
        LOG_NOTIFICATION_CONTROL(0x06);

        companion object {
            fun from(target: Int?): DkbErrorType? {
                return DkbErrorType.values().find { code -> code.errorType == target }
            }
        }
    }

    /**
     * 共通エラー エラー内容
     */
    enum class DkbErrorInformation(val errorInfo: Int) {
        FORMAT_OR_SEQUENCE_ERROR(0x01),
        TIMEOUT(0x02),
        OUTDATED(0x03),
        ROLLING_COUNT(0x04),
        ILLEGAL_OPERATION_AUTHORITY(0x05),
        ILLEGAL_USER(0x06),
        ILLEGAL_ACCESS(0x07),
        INVALID_AUTH_CODE(0x08),
        DKB_FUNC_REQUEST_ERROR(0x09),
        DKB_CONTROL_COMMAND_ERROR(0x0A),
        RETURN_CODE_ERROR(0x0B),
        SERVER_ERROR(0x0C),
        INVALID_SERVER(0x0D),
        SERVER_CONNECTION_ERROR(0x0E),
        PUT_BACK_FAILED(0x0F),
        REPLAY_COUNT(0x10),
        IN_USE(0x11),
        READ_LOG_FAILED(0x12),
        FDS_WRITE_FAILED(0x13),
        LOG_DATA_OVERFLOW(0x14),
        ILLEGAL_LOG_TYPE(0x15);

        companion object {
            fun from(target: Int?): DkbErrorInformation? {
                return DkbErrorInformation.values().find { code -> code.errorInfo == target }
            }
        }
    }
}