package jp.co.tokairika.cryptogw.sdkaccessor

import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.command.DkCommandResult
import jp.co.tokairika.cryptogw.command.SimpleDkCommandResult
import jp.co.tokairika.cryptogw.command.StatusDkCommand
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.manager.exception.DkCommandFailedException
import jp.co.tokairika.cryptogw.utils.toHexString

internal class LockDeviceController(
    private val sequenceName: String,
    private val logOperationRepository: LogOperationRepository,
    private val lockDeviceTimeDataNotifier: LockDeviceTimeDataNotifier
) {

    suspend fun control(
        connection: LockDeviceConnection,
        dkCommands: List<DkCommand>
    ): List<DkCommandResult> {
        if (dkCommands.any { it.needsNotifyTimeDataBeforeExecute }) {
            notifyTimeDataIfNeeded(connection)
        }

        val results = mutableListOf<DkCommandResult>()

        for (dkCommand in dkCommands) {
            val result = try {
                execute(connection, dkCommand)
            } catch (exception: Exception) {
                results.add(SimpleDkCommandResult(isSucceeded = false, statusCode = null))
                throw DkCommandFailedException(exception, results)
            }

            results.add(result)

            if (!result.isSucceeded) {
                val exception =
                    DkCommandFailedException(ResultType.COMMAND_FAILED.detail(), results)
                logOperationRepository.create(
                    functionName = "${sequenceName}.parseDkCommandResult",
                    sequenceName = sequenceName,
                    lockId = connection.cryptoGwDigitalKey.lockId,
                    exception = exception,
                    oneTimeKeyId = connection.cryptoGwDigitalKey.otkId,
                    command = dkCommand.controlCode.toHexString(),
                    dkbResponseData = result.statusCode.toHexString()
                )
                throw exception
            }
        }

        return results
    }

    /**
     * DKコマンドを実行する。
     *
     * @param dkCommand DKコマンド
     * @return DKコマンド結果
     */
    private suspend fun execute(
        connection: LockDeviceConnection, dkCommand: DkCommand
    ): DkCommandResult {
        var isSyncTimeSuccess = false
        var dkCommandResult = connection.control(dkCommand)
        if (dkCommandResult.needsNotifyTimeData) {
            isSyncTimeSuccess = lockDeviceTimeDataNotifier.syncTime(connection)
        }
        if (!dkCommandResult.isSucceeded && isSyncTimeSuccess) {
            dkCommandResult = connection.control(dkCommand)
        }
        return dkCommandResult
    }


    private suspend fun notifyTimeDataIfNeeded(connection: LockDeviceConnection) {
        try {
            val statusDkCommandResult = connection.control(StatusDkCommand())
            if (statusDkCommandResult.needsNotifyTimeData) {
                lockDeviceTimeDataNotifier.syncTime(connection)
            }
        } catch (e: Exception) {
            return
        }
    }
}