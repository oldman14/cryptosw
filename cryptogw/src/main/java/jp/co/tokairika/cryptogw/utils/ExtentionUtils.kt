package jp.co.tokairika.cryptogw.utils


/**
 * 16 進文字列をバイト配列に変換する
 * @param? String
 * @return {ByteArray}
 */
internal fun String?.hexToByteArrays(): ByteArray {
    return if (this.isNullOrEmpty()) byteArrayOf()
    else {
        this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}

/**
 * null または空の文字列を確認する
 */
internal fun String?.takeIfEmpty(default: String = ""): String {
    if (this.isNullOrEmpty()) {
        return default
    }
    return this
}

internal fun Exception?.takeIfEmpty(default: String = ""): String {
    if (this == null) {
        return default
    }
    return this.toString()
}

internal fun List<String>?.joinToStringCommas(): String {
    if (this.isNullOrEmpty()) return ""
    return this.joinToString(",")
}

/**
 * バイト配列を 16 進文字列に変換する
 * @param? {ByteArray}
 * @return String
 */
internal fun ByteArray?.toHexString(): String = this?.joinToString("") { "%02x".format(it) } ?: ""
