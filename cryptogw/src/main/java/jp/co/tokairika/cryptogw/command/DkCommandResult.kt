package jp.co.tokairika.cryptogw.command


/**
 * 成功したかどうかのみを保持する DK コマンドの結果
 */
interface DkCommandResult {
    /** コマンドが成功したか **/
    val isSucceeded: Boolean

    /** ステータスコード。DKから返却されるバイト値 */
    val statusCode: ByteArray?

    val needsSendDkLog: Boolean
        get() = false

    val needsNotifyTimeData: Boolean
        get() = false
}
