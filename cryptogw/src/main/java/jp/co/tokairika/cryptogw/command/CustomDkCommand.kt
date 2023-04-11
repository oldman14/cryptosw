package jp.co.tokairika.cryptogw.command

/**
 * カスタムDKコマンド
 */
abstract class CustomDkCommand : DkCommand {

    /**
     * 時刻同期が必要かを返却する。
     * trueである場合、DKコマンドを実行する前に時刻同期が必要かDKに問い合わせる。
     */
    override val needsNotifyTimeDataBeforeExecute: Boolean = true

    /**
     * ステータスコードをDKコマンド実行結果に変換する。
     * @param {ByteArray?} ステータスコード。DKから返却されるバイト値
     * @return DKコマンド実行結果
     */
    override fun parseDkCommandResult(statusCode: ByteArray): DkCommandResult {
        val isSucceeded = statusCode.getOrNull(0)?.toInt().let {
            it == 0x00 || it == 0x01
        }

        return SimpleDkCommandResult(isSucceeded = isSucceeded, statusCode = statusCode)
    }

}