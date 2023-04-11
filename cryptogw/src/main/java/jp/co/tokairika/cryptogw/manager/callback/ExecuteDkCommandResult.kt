package jp.co.tokairika.cryptogw.manager.callback

import jp.co.tokairika.cryptogw.command.DkCommandResult
import jp.co.tokairika.cryptogw.manager.CryptoGwResult
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.manager.exception.DkCommandFailedException


/**
 * DKコマンドを実行した結果
 */
class ExecuteDkCommandResult(
    resultType: ResultType,
    message: String,
    val dkCommandResult: DkCommandResult? = null
) : CryptoGwResult(resultType, message) {

    companion object {
        internal fun build(exception: Exception): ExecuteDkCommandResult {
            val cryptoGwResult = CryptoGwResult.build(exception)
            val dkCommandResult = (exception as? DkCommandFailedException?)?.dkCommandResults?.firstOrNull()
            return ExecuteDkCommandResult(
                resultType = cryptoGwResult.resultType,
                message = cryptoGwResult.message,
                dkCommandResult = dkCommandResult
            )
        }
    }
}