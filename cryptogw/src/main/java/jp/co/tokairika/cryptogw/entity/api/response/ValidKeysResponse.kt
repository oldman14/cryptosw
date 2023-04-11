package jp.co.tokairika.cryptogw.entity.api.response

internal data class ValidKeysResponse(
    val validatedDigitalKeyResults: List<ValidatedKeyResult>
)

internal data class ValidatedKeyResult(
    val digitalKeyId: String,
    val isValid: Boolean,
    val isDeleted: Boolean
)

