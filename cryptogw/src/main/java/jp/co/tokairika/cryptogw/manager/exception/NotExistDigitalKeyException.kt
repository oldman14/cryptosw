package jp.co.tokairika.cryptogw.manager.exception

internal class NotExistDigitalKeyException : Exception() {
    override fun toString(): String {
        return "NOT_EXIST_DIGITAL_KEY"
    }
}