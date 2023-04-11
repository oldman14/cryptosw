package jp.co.tokairika.cryptogw.entity.api.request

internal data class IssueOneTimeKeyRequest(
    val sdkId: String,
    val grantId: String,
    val oneTimeKeyRequestToken: String,
)

