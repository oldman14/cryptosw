package jp.co.tokairika.cryptogw.manager.callback

import jp.co.tokairika.cryptogw.manager.CryptoGwResult


/**
 * DK初期化処理のインターフェース
 */
interface CryptoGWCallback {
    /**
     * 処理完了
     *
     * @param result 処理結果
     */
    fun onReceived(result: CryptoGwResult)
}