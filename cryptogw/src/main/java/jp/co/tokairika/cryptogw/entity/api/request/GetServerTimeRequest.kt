package jp.co.tokairika.cryptogw.entity.api.request

internal data class GetServerTimeRequest(
    val sdkId: String,
    val publishId: String,
    val requestTimeData: String
)