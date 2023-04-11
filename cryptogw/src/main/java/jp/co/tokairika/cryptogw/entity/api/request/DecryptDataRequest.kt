package jp.co.tokairika.cryptogw.entity.api.request

internal data class DecryptDataRequest(
    val dkbSerialNo: String,
    val encryptionData: String,
    val format: String = "1",
    val encryptSoftLibrarySelectCode: String = "EXT_ENC_LIB_STG",
    val encryptSoftLogicSelectCode: String = "1",
)
