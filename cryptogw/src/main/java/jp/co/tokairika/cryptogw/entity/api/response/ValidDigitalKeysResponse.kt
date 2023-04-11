package jp.co.tokairika.cryptogw.entity.api.response

internal data class ValidDigitalKeysResponse(
    val validatedDigitalKeyResults: List<ValidatedDigitalKeyResult>,
    val digitalKeyIssuableGrants: List<DigitalKeyIssuableGrants>,
    val error: Errors?
)

internal data class ValidatedDigitalKeyResult(
    val digitalKeyId: String,
    val isDeleted: Boolean
)

internal data class DigitalKeyIssuableGrants(
    val grantId: String?,
    val digitalKeyId: String?,
    val issuableStartAt: String?,
    val issuableEndAt: String?,
    val reissue: Boolean?
)
