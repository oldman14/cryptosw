package jp.co.tokairika.cryptogw.manager

import android.util.Base64
import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.jobworker.*
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWCallback
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandCallback
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandsCallback
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.sdkaccessor.DigitalKeyDeleter
import jp.co.tokairika.cryptogw.sdkaccessor.DkConnectOption
import jp.co.tokairika.cryptogw.security.GwKeyStore
import jp.co.tokairika.cryptogw.usecase.accessor.ExecuteCommandUseCase
import jp.co.tokairika.cryptogw.usecase.manager.DeleteAllUseCase
import jp.co.tokairika.cryptosdk.DkManager
import kotlinx.coroutines.*

internal class CryptoGwManagerImp constructor(
    private val dkConnectOption: DkConnectOption,
    private val dkManager: DkManager,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository,
    private val cryptoGwState: CryptoGwState,
    private val cryptoGWApiService: CryptoGWApiService,
    private val coroutineDispatcher: CoroutineDispatcher,
) : CryptoGwManager {

    private val digitalKeyDeleter = DigitalKeyDeleter(digitalKeyRepository, dkManager)

    private val handlerException = CoroutineExceptionHandler { _, exception ->
        println("handlerException: $exception ")
    }

    /** 非同期処理用のSDK独自coroutineScope */
    private val cryptoGWCoroutineScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob() + handlerException)

    private val jobWorkerQueue = JobWorkerQueue(cryptoGWCoroutineScope)

    /**
     * @see CryptoGwManager.initialize
     */
    override fun initialize(
        callback: CryptoGWCallback
    ) {
        val initializeJobWorker =
            InitializeJobWorker(
                cryptoGwState = cryptoGwState,
                logOperationRepository = logOperationRepository,
                cryptoGWApiService = cryptoGWApiService,
                dkManager = dkManager,
                coroutineDispatcher = coroutineDispatcher,
                callback = callback
            )
        jobWorkerQueue.post(initializeJobWorker)
    }

    /**
     * @see CryptoGwManager.pullDigitalKeysByGrant
     */
    override fun pullDigitalKeysByGrant(
        grantId: String,
        callback: CryptoGWCallback
    ) {
        val pullDigitalKeyJobWorker = PullDigitalKeyJobWorker(
            cryptoGwState = cryptoGwState,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository,
            cryptoGWApiService = cryptoGWApiService,
            dkManager = dkManager,
            digitalKeyDeleter = digitalKeyDeleter,
            grantId = grantId,
            lockId = null,
            sequenceName = "pullDigitalKeysByGrant",
            coroutineDispatcher = coroutineDispatcher,
            callback = callback
        )
        jobWorkerQueue.post(pullDigitalKeyJobWorker)

    }

    /**
     * @see CryptoGwManager.pullDigitalKeysByLock
     */
    override fun pullDigitalKeysByLock(
        lockId: String,
        callback: CryptoGWCallback
    ) {
        val pullDigitalKeyJobWorker = PullDigitalKeyJobWorker(
            cryptoGwState = cryptoGwState,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository,
            cryptoGWApiService = cryptoGWApiService,
            dkManager = dkManager,
            digitalKeyDeleter = digitalKeyDeleter,
            grantId = null,
            lockId = lockId,
            sequenceName = "pullDigitalKeysByLock",
            coroutineDispatcher = coroutineDispatcher,
            callback = callback
        )
        jobWorkerQueue.post(pullDigitalKeyJobWorker)
    }

    /**
     * @see CryptoGwManager.executeDkCommandByGrant
     */
    override fun executeDkCommandByGrant(
        grantId: String,
        dkCommand: DkCommand,
        callback: ExecuteDkCommandCallback
    ) {
        val executeDkCommandJobWorker = ExecuteDkCommandJobWorker(
            sequenceName = "executeDkCommandByGrant",
            cryptoGwState = cryptoGwState,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository,
            cryptoGWApiService = cryptoGWApiService,
            digitalKeyDeleter = digitalKeyDeleter,
            dkConnectOption = dkConnectOption,
            dkCommands = listOf(dkCommand),
            executeBy = ExecuteCommandUseCase.ExecuteBy.Grant,
            grantId = grantId,
            lockId = "",
            coroutineDispatcher = coroutineDispatcher,
            callback = callback
        )
        jobWorkerQueue.post(executeDkCommandJobWorker)
    }

    /**
     * @see CryptoGwManager.executeDkCommandByLock
     */
    override fun executeDkCommandByLock(
        lockId: String,
        dkCommand: DkCommand,
        callback: ExecuteDkCommandCallback,
    ) {
        val executeDkCommandJobWorker = ExecuteDkCommandJobWorker(
            sequenceName = "executeDkCommandByLock",
            cryptoGwState = cryptoGwState,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository,
            cryptoGWApiService = cryptoGWApiService,
            digitalKeyDeleter = digitalKeyDeleter,
            dkConnectOption = dkConnectOption,
            dkCommands = listOf(dkCommand),
            executeBy = ExecuteCommandUseCase.ExecuteBy.Lock,
            lockId = lockId,
            grantId = "",
            coroutineDispatcher = coroutineDispatcher,
            callback = callback
        )
        jobWorkerQueue.post(executeDkCommandJobWorker)

    }

    /**
     * @see CryptoGwManager.executeDkCommandsByGrant
     */
    override fun executeDkCommandsByGrant(
        grantId: String,
        dkCommands: List<DkCommand>,
        callback: ExecuteDkCommandsCallback
    ) {
        cryptoGWCoroutineScope.launch {

            val executeDkCommandsJobWorker = ExecuteDkCommandsJobWorker(
                sequenceName = "executeDkCommandsByGrant",
                cryptoGwState = cryptoGwState,
                logOperationRepository = logOperationRepository,
                digitalKeyRepository = digitalKeyRepository,
                cryptoGWApiService = cryptoGWApiService,
                digitalKeyDeleter = digitalKeyDeleter,
                dkConnectOption = dkConnectOption,
                dkCommands = dkCommands,
                executeBy = ExecuteCommandUseCase.ExecuteBy.Grant,
                grantId = grantId,
                lockId = "",
                coroutineDispatcher = coroutineDispatcher,
                callback = callback
            )
            jobWorkerQueue.post(executeDkCommandsJobWorker)
        }
    }

    /**
     * @see CryptoGwManager.executeDkCommandsByLock
     */
    override fun executeDkCommandsByLock(
        lockId: String,
        dkCommands: List<DkCommand>,
        callback: ExecuteDkCommandsCallback
    ) {
        cryptoGWCoroutineScope.launch {
            val executeDkCommandsJobWorker =
                ExecuteDkCommandsJobWorker(
                    sequenceName = "executeDkCommandsByLock",
                    cryptoGwState = cryptoGwState,
                    logOperationRepository = logOperationRepository,
                    digitalKeyRepository = digitalKeyRepository,
                    cryptoGWApiService = cryptoGWApiService,
                    digitalKeyDeleter = digitalKeyDeleter,
                    dkConnectOption = dkConnectOption,
                    dkCommands = dkCommands,
                    executeBy = ExecuteCommandUseCase.ExecuteBy.Lock,
                    lockId = lockId,
                    grantId = "",
                    coroutineDispatcher = coroutineDispatcher,
                    callback = callback
                )
            jobWorkerQueue.post(executeDkCommandsJobWorker)
        }
    }

    /**
     * @see CryptoGwManager.getDigitalKeysByGrantId
     */
    override fun getDigitalKeysByGrantId(grantId: String): List<CryptoGwDigitalKey> {
        val digitalKeyRepository = DigitalKeyRepository()
        return digitalKeyRepository.readByGrantId(grantId)
    }

    /**
     * @see CryptoGwManager.getAllDigitalKeys
     */
    override fun getAllDigitalKeys(): List<CryptoGwDigitalKey> {
        val digitalKeyRepository = DigitalKeyRepository()
        return digitalKeyRepository.readAll()
    }

    /**
     * @see CryptoGwManager.deleteDigitalKeysByGrantId
     */
    override fun deleteDigitalKeysByGrantId(
        grantId: String,
    ) {
        val digitalKeyDeleter = DigitalKeyDeleter(digitalKeyRepository, dkManager)
        digitalKeyDeleter.removeAllKeysOfGrant(grantId)
    }

    /**
     * @see CryptoGwManager.deleteDigitalKeysByLockId
     */
    override fun deleteDigitalKeysByLockId(
        lockId: String,
    ) {
        val digitalKeyDeleter = DigitalKeyDeleter(digitalKeyRepository, dkManager)
        digitalKeyDeleter.removeAllKeysOfLock(lockId)
    }

    /**
     * @see CryptoGwManager.deleteAllDigitalKeys
     */
    override fun deleteAllDigitalKeys() {
        val digitalKeyDeleter = DigitalKeyDeleter(digitalKeyRepository, dkManager)
        digitalKeyDeleter.removeAllKeys()
    }

    /**
     * @see CryptoGwManager.setToken
     */
    override fun setToken(token: String) {
        GwPreferenceUtils.sdkToken = GwKeyStore().encrypt(token.toByteArray(), GwKeyStore.Alias.SDK_TOKEN)
    }

    /**
     * @see CryptoGwManager.deleteToken
     */
    override fun deleteToken() {
        GwPreferenceUtils.sdkToken = ""
    }

    /**
     * @see CryptoGwManager.deleteSdkId
     */
    override fun deleteSdkId() {
        GwPreferenceUtils.sdkId = ""
    }

    /**
     * @see CryptoGwManager.deleteAll
     */
    override fun deleteAll(
    ) {
        val useCase = DeleteAllUseCase(
            dkManager = dkManager,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository
        )
        useCase.deleteAll()
    }

    override fun clearSharedKeyConfig() {
        GwPreferenceUtils.lastDateCallMobileSetting = ""
    }
}
