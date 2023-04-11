package jp.co.tokairika.cryptogw.manager.exception

internal class OutOfTermDigitalKeyException : Exception() {
    override fun toString(): String {
        return "OUT_OF_TERM_DIGITAL_KEY"
    }
}