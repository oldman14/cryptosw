package jp.co.tokairika.cryptogw.entity.api.request

internal data class ValidateDigitalKeyByGrantOrLockRequest(
    val sdkId: String,
    val lockId: String? = null,
    val grantId: String? = null,
    val digitalKeyIds: List<String>
)