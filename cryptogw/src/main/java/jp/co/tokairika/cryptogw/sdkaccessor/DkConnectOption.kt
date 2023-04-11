package jp.co.tokairika.cryptogw.sdkaccessor

data class DkConnectOption(
    val timeout: Timeout = Timeout.FIFTEEN
) {
    enum class Timeout(val value: Int) {
        FIVE(5_000),
        TEN(10_000),
        FIFTEEN(15_000),
        THIRTY(30_000),
        SIXTY(60_000)
    }
}