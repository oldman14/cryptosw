package jp.co.tokairika.cryptogw.entity.api.response

//ワンタイムキー発行

internal data class IssueOneTimeKeyResponse(
    val publishId: String?,
    val keyData: String?,
    val serviceUuid: String?,
    val bleDeviceId: String?,
    val oneTimeKeyRequestToken: String?,
    val writeCharacteristicUuid: String?,
    val indicationCharacteristicUuid: String?,
    val lockId: String?,
    val grantId: String?,
    val useStartDate: String?,
    val useEndDate: String?,
    val slotId: String?,
    val publishDate: String?,
    )
