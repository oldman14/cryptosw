package jp.co.tokairika.cryptogw.manager.callback


/**
 * DKコマンドの実行結果を取得するコールバック
 */
interface ExecuteDkCommandsCallback {
    /**
     * 実行結果を取得した場合、実行される。
     *
     * @param {ExecuteDkCommandResult} result
     */
    fun onReceived(result: ExecuteDkCommandsResult)
}