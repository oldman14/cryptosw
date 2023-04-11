package jp.co.tokairika.cryptogw.command

/**
 * 成功したかだけを保持する簡易なDKコマンド結果
 */
internal class SimpleDkCommandResult(
    override val isSucceeded: Boolean,
    override val statusCode: ByteArray?
) : DkCommandResult