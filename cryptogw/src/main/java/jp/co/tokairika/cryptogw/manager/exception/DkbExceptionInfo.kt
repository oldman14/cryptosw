package jp.co.tokairika.cryptogw.manager.exception


internal class DkbExceptionInfo(info: ByteArray) {
    val dkbErrorType: DkbErrorNotification.DkbErrorType? = info.getOrNull(11)?.toInt()
        .let { DkbErrorNotification.DkbErrorType.from(it) }

    val dkbErrorInformation: DkbErrorNotification.DkbErrorInformation? = info.getOrNull(13)?.toInt()
        .let { DkbErrorNotification.DkbErrorInformation.from(it) }

    val cryptoGwErrorType: DkbErrorNotification.CryptoGwErrorType =
        DkbErrorNotification.CryptoGwErrorType.from(
            dkbErrorType = dkbErrorType,
            dkbErrorInformation = dkbErrorInformation
        )
}