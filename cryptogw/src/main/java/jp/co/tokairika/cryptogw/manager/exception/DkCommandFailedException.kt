package jp.co.tokairika.cryptogw.manager.exception

import jp.co.tokairika.cryptogw.command.DkCommandResult
import jp.co.tokairika.cryptogw.utils.toHexString

/**
 * DKコマンド実行が失敗した
 */
internal class DkCommandFailedException : Exception {
    val dkCommandResults: List<DkCommandResult>

    constructor(message: String, dkCommandResults: List<DkCommandResult>) : super(message) {
        this.dkCommandResults = dkCommandResults
    }

    constructor(cause: Exception, dkCommandResults: List<DkCommandResult>) : super(cause) {
        this.dkCommandResults = dkCommandResults
    }

    override fun toString(): String {
        var data = "COMMAND_FAILED:["
        var spaceChar = ""
        dkCommandResults.mapIndexed { index, dkCommandResult ->
            spaceChar = if (index > 0) " - " else "- "
            data += "$spaceChar${dkCommandResult.statusCode.toHexString()}"
        }
        data += " -]"
        return data
    }
}