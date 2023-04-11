package jp.co.tokairika.cryptogw.jobworker

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWCallback
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.usecase.manager.InitializeUseCase
import jp.co.tokairika.cryptosdk.DkManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class InitializeJobWorker(
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val dkManager: DkManager,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val callback: CryptoGWCallback,
) : JobWorker {

    override suspend fun handle() {
        val useCase = InitializeUseCase(cryptoGwState, logOperationRepository, cryptoGWApiService, dkManager, GwPreferenceUtils)
        val result = useCase.initialize()
        coroutineScope {
            launch(coroutineDispatcher) {
                callback.onReceived(result)
            }
        }
    }
}