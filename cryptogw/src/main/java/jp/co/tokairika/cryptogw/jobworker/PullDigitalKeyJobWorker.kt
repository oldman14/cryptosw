package jp.co.tokairika.cryptogw.jobworker

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWCallback
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.sdkaccessor.DigitalKeyDeleter
import jp.co.tokairika.cryptogw.usecase.manager.PullDigitalKeyUseCase
import jp.co.tokairika.cryptosdk.DkManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class PullDigitalKeyJobWorker(
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val dkManager: DkManager,
    private val digitalKeyDeleter: DigitalKeyDeleter,
    private val lockId: String?,
    private val grantId: String?,
    private val sequenceName: String,
    private val callback: CryptoGWCallback,
    private val coroutineDispatcher: CoroutineDispatcher,
) : JobWorker {

    override suspend fun handle() {
        val useCase = PullDigitalKeyUseCase(
            cryptoGwState = cryptoGwState,
            logOperationRepository = logOperationRepository,
            digitalKeyRepository = digitalKeyRepository,
            cryptoGWApiService = cryptoGWApiService,
            dkManager = dkManager,
            preferenceUtils = GwPreferenceUtils,
            digitalKeyDeleter = digitalKeyDeleter,
            grantId = grantId,
            lockId = lockId,
            sequenceName = sequenceName
        )
        val result = useCase.pullDigitalKey()
        coroutineScope {
            launch(coroutineDispatcher) { callback.onReceived(result) }
        }
    }

}