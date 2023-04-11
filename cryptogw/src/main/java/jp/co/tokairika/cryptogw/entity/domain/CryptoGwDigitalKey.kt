package jp.co.tokairika.cryptogw.entity.domain


data class CryptoGwDigitalKey(
    val otkId: String,
    val grantId: String,
    val lockId: String,
    val bleDeviceId: String,
    val serviceUuid: String,
    val useStartDate: String,
    val useEndDate: String,
    val slotId: String,
    val publishDate: String
)


