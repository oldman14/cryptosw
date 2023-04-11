package jp.co.tokairika.cryptogw.entity.api.request

internal data class ShareKeyRequest(
    val sdkId: String,
    val clientHandshakeMessage: String
)