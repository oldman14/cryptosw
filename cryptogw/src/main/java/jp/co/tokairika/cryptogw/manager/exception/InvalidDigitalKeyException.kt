package jp.co.tokairika.cryptogw.manager.exception

internal class InvalidDigitalKeyException(message: String) : Exception(message) {
    override fun toString(): String {
        return if (message.isNullOrEmpty()) "INVALID_DIGITAL_KEY" else message
    }
}