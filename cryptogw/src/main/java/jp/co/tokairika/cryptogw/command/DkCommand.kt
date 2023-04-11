package jp.co.tokairika.cryptogw.command

/**
 * DKコマンド.
 * DKに対して実行するコマンドのインタフェース
 */
interface DkCommand {
    /**
     * 制御コード.
     * DKに送信するリクエストのバイト値である。
     */
    val controlCode: ByteArray

    /**
     * 時刻同期が必要かを返却する。
     * trueである場合、DKコマンドを実行する前に時刻同期が必要かDKに問い合わせる。
     */
    val needsNotifyTimeDataBeforeExecute: Boolean

    /**
     * ステータスコードをDKコマンド実行結果に変換する。
     * @param {ByteArray?} ステータスコード。DKから返却されるバイト値
     * @return DKコマンド実行結果
     */
    fun parseDkCommandResult(statusCode: ByteArray): DkCommandResult
}
