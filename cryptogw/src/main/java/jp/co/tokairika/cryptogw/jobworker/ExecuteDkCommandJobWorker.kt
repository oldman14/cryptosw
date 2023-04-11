package jp.co.tokairika.cryptogw.jobworker

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandCallback
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandResult
import jp.co.tokairika.cryptogw.sdkaccessor.DigitalKeyDeleter
import jp.co.tokairika.cryptogw.sdkaccessor.DkConnectOption
import jp.co.tokairika.cryptogw.usecase.accessor.ExecuteCommandUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class ExecuteDkCommandJobWorker(
    private val dkConnectOption: DkConnectOption,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val grantId: String,
    private val lockId: String,
    private val dkCommands: List<DkCommand>,
    private val callback: ExecuteDkCommandCallback,
    private val sequenceName: String,
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val digitalKeyDeleter: DigitalKeyDeleter,
    private val executeBy: ExecuteCommandUseCase.ExecuteBy
) : JobWorker {
    override suspend fun handle() {
        val useCase = ExecuteCommandUseCase(
            sequenceName = sequenceName,
            cryptoGwState = cryptoGwState,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository,
            cryptoGWApiService = cryptoGWApiService,
            digitalKeyDeleter = digitalKeyDeleter,
            dkConnectOption = dkConnectOption,
            dkCommands = dkCommands,
            executeBy = executeBy,
            lockId = lockId,
            grantId = grantId
        )

        val results = useCase.executeCommands()
        val result = ExecuteDkCommandResult(
            resultType = results.resultType,
            message = results.message,
            dkCommandResult = results.dkCommandResults.firstOrNull()
        )
        coroutineScope {
            this.launch(coroutineDispatcher) { callback.onReceived(result) }
        }
    }
}