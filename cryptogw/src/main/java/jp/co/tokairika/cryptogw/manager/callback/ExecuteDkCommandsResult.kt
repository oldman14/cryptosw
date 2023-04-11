package jp.co.tokairika.cryptogw.manager.callback

import jp.co.tokairika.cryptogw.command.DkCommandResult
import jp.co.tokairika.cryptogw.manager.CryptoGwResult
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.manager.exception.DkCommandFailedException


/** * DKコマンドリストを実行した結果 */
class ExecuteDkCommandsResult : CryptoGwResult {
    companion object {
        internal fun build(exception: Exception): ExecuteDkCommandsResult {
            val cryptoGWResult = CryptoGwResult.build(exception)
            val dkCommandFailedException = exception as? DkCommandFailedException
            return ExecuteDkCommandsResult(
                resultType = cryptoGWResult.resultType,
                message = cryptoGWResult.message,
                dkCommandResults = dkCommandFailedException?.dkCommandResults ?: listOf()
            )
        }
    }

    val dkCommandResults: List<DkCommandResult>

    internal constructor(dkCommandResults: List<DkCommandResult>) : super(ResultType.SUCCESS) {
        this.dkCommandResults = dkCommandResults
    }

    private constructor(
        resultType: ResultType,
        message: String,
        dkCommandResults: List<DkCommandResult>
    ) : super(resultType, message) {
        this.dkCommandResults = dkCommandResults
    }
}