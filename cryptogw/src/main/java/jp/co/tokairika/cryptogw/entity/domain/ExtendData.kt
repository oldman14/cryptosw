package jp.co.tokairika.cryptogw.entity.domain

class ExtendData(
    val dkbSerialNo: String = "",
    val byteDataSend: String = "",
    var byteDataReceive: String = "",
    var header: ByteArray? = null,
    val isServerEncrypted: Boolean = false,
    var bytePadding: Int = 0
)