package jp.co.tokairika.cryptogw.usecase.manager

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.manager.CryptoGwResult
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.security.GwKeyStore
import jp.co.tokairika.cryptogw.utils.DateUtils
import jp.co.tokairika.cryptosdk.DkManager
import java.util.concurrent.TimeUnit

internal class InitializeUseCase(
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val dkManager: DkManager,
    private val preferenceUtils: GwPreferenceUtils
) {

    /**
     * CryptoGW の初期化
     * @return DK関連処理結果クラス
     */
    suspend fun initialize(): CryptoGwResult {
        try {
            if (cryptoGwState.isInitialized) {
                logOperationRepository.create(
                    sequenceName = "initialize",
                    functionName = "Initialize Again",
                    lockId = "",
                    exception = null
                )
                updateShareKeyIfNeeded()
                return CryptoGwResult(ResultType.SUCCESS)
            } else {
                val resultSdkId = cryptoGWApiService.issueSdkId()
                resultSdkId?.sdkId?.let { sdkId ->
                    preferenceUtils.sdkId = sdkId
                    val requestData = dkManager.generatePublishKey()
                    val resultSharedKey = cryptoGWApiService.shareKey(requestData)
                    resultSharedKey?.serverHandshakeMessage?.let { severPublicKey ->
                        dkManager.storeSharedKey(severPublicKey)
                        preferenceUtils.timeStoreShareKey = DateUtils.currentTime()
                        logOperationRepository.create(
                            sequenceName = "initialize",
                            functionName = "initialize",
                            lockId = "",
                            exception = null
                        )
                        return CryptoGwResult(ResultType.SUCCESS)
                    }
                }
                return CryptoGwResult(ResultType.UNEXPECTED_ERROR)
            }
        } catch (e: IllegalStateException) {
            return CryptoGwResult.build(e)
        } catch (e: IllegalArgumentException) {
            return CryptoGwResult.build(e)
        } catch (e: Exception) {
            logOperationRepository.create(
                sequenceName = "initialize",
                functionName = "InitializeUseCase.initialize",
                lockId = "",
                exception = e
            )
            return CryptoGwResult.build(e)
        } finally {
            logOperationRepository.sendToServer()
        }
    }

    private suspend fun updateShareKeyIfNeeded() {
        try {
            val currentTime = DateUtils.currentTime()
            val currentDate = DateUtils.convertTimeToString(currentTime)
            if (preferenceUtils.lastDateCallMobileSetting != currentDate) {
                val sharedKeyUpdateInterval =
                    cryptoGWApiService.mobileSettings()?.sharedKeyUpdateInterval
                preferenceUtils.sharedKeyUpdateInterval = sharedKeyUpdateInterval ?: 0
                preferenceUtils.lastDateCallMobileSetting = currentDate
            }
            val sharedKeyUpdateIntervalTimeInMillis =
                preferenceUtils.sharedKeyUpdateInterval * TimeUnit.DAYS.toMillis(1)
            if (preferenceUtils.timeStoreShareKey + sharedKeyUpdateIntervalTimeInMillis < currentTime) {
                val requestData = dkManager.generatePublishKey()
                val resultSharedKey =
                    cryptoGWApiService.shareKey(requestData)
                resultSharedKey?.serverHandshakeMessage?.let { severPublicKey ->
                    dkManager.storeSharedKey(severPublicKey)
                    preferenceUtils.timeStoreShareKey = currentTime
                }
            }

        } catch (e: Exception) {
            return
        }
    }
}