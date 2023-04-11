package jp.co.tokairika.cryptogw.command

/**
 * 基本DKコマンド結果
 */
internal class BasicDkCommandResult(
    override val isSucceeded: Boolean,
    override val statusCode: ByteArray?,
    override val needsSendDkLog: Boolean,
    override val needsNotifyTimeData: Boolean
) : DkCommandResult