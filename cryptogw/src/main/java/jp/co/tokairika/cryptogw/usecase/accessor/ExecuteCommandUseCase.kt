package jp.co.tokairika.cryptogw.usecase.accessor

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandsResult
import jp.co.tokairika.cryptogw.manager.exception.CryptoGwNotInitializedException
import jp.co.tokairika.cryptogw.sdkaccessor.*
import jp.co.tokairika.cryptogw.utils.joinToStringCommas
import jp.co.tokairika.cryptogw.utils.takeIfEmpty
import jp.co.tokairika.cryptogw.utils.toHexString

internal class ExecuteCommandUseCase(
    private val sequenceName: String,
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val digitalKeyDeleter: DigitalKeyDeleter,
    private val dkCommands: List<DkCommand>,
    private val lockId: String?,
    private val grantId: String?,
    private val executeBy: ExecuteBy,
    dkConnectOption: DkConnectOption,
) {
    private val lockDeviceScanner = LockDeviceScanner(
        sequenceName = sequenceName,
        dkCommands = dkCommands,
        logOperationRepository = logOperationRepository,
        dkConnectOption = dkConnectOption
    )
    private val lockDeviceTimeDataNotifier = LockDeviceTimeDataNotifier(
        sequenceName = sequenceName,
        logOperationRepository = logOperationRepository,
        cryptoGWApiService = cryptoGWApiService
    )
    private val lockDeviceConnector =
        LockDeviceConnector(
            digitalKeyDeleter = digitalKeyDeleter,
            sequenceName = sequenceName,
            dkConnectOption = dkConnectOption,
            logOperationRepository = logOperationRepository
        )
    private val lockDeviceController = LockDeviceController(
        sequenceName = sequenceName,
        logOperationRepository = logOperationRepository,
        lockDeviceTimeDataNotifier = lockDeviceTimeDataNotifier
    )

    /**
     * DKコマンドリストを実行する。
     *
     * @return DKコマンドリスト実行結果
     */
    suspend fun executeCommands(): ExecuteDkCommandsResult {
        var connection: LockDeviceConnection? = null
        return try {
            if (!cryptoGwState.isInitialized) {
                logOperationRepository.create(
                    functionName = sequenceName,
                    sequenceName = sequenceName,
                    command = dkCommands.map { it.controlCode.toHexString() }.joinToStringCommas(),
                    exception = CryptoGwNotInitializedException(),
                )
                throw CryptoGwNotInitializedException()
            }
            val digitalKeyValidator = DigitalKeyValidator(
                sequenceName = sequenceName,
                dkCommands = dkCommands,
                deleter = digitalKeyDeleter,
                cryptoGwState = cryptoGwState,
                logOperationRepository = logOperationRepository,
                digitalKeyRepository = digitalKeyRepository,
                cryptoGWApiService = cryptoGWApiService,
                grantId = grantId.takeIfEmpty(),
                lockId = lockId.takeIfEmpty(),
                executeBy = executeBy
            )
            val keysValid = digitalKeyValidator.getAvailableOneTimeKeys()
            val bleDevice = lockDeviceScanner.startScan(keysValid)
            connection = lockDeviceConnector.connect(keysValid, dkCommands, bleDevice)
            val dkCommandResults = lockDeviceController.control(connection, dkCommands)
            ExecuteDkCommandsResult(dkCommandResults = dkCommandResults)
        } catch (exception: Exception) {
            ExecuteDkCommandsResult.build(exception)
        } finally {
            connection?.disconnect()
            logOperationRepository.sendToServer()
        }
    }

    enum class ExecuteBy {
        Grant, Lock
    }
}
