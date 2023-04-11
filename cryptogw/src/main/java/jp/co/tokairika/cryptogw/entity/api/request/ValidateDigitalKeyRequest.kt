package jp.co.tokairika.cryptogw.entity.api.request

internal data class ValidateDigitalKeyRequest(
    val sdkId: String,
    val digitalKeyIds: List<String>
)