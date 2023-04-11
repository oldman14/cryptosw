package jp.co.tokairika.cryptogw.usecase.manager

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.manager.CryptoGwResult
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.manager.exception.CryptoGwNotInitializedException
import jp.co.tokairika.cryptogw.manager.exception.IssuableDigitalKeysLimitException
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.sdkaccessor.DigitalKeyDeleter
import jp.co.tokairika.cryptogw.utils.joinToStringCommas
import jp.co.tokairika.cryptosdk.DkManager

internal class PullDigitalKeyUseCase(
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val dkManager: DkManager,
    private val preferenceUtils: GwPreferenceUtils,
    private val digitalKeyDeleter: DigitalKeyDeleter,
    private val lockId: String?,
    private val grantId: String?,
    private val sequenceName: String
) {

    /**
     *  サーバーから新しいワンタイム キーをダウンロードする
     *  @param grantId GRANT ID
     *  @return ワンタイムキー情報
     */
    private suspend fun downloadKey(
        grantId: String,
    ): CryptoGwDigitalKey {
        val requestToken = dkManager.generateOneTimeRequestToken()
        val result = cryptoGWApiService.issueOneTimeKey(
            grantId,
            requestToken,
        )
        dkManager.storeDkbAccessInfo(
            oneTimeKeyId = result?.publishId,
            keyData = result?.keyData,
            serviceUuid = result?.serviceUuid.removeHyphen(),
            writeCharacteristicUuid = result?.writeCharacteristicUuid?.removeHyphen(),
            indicationCharacteristicUuid = result?.indicationCharacteristicUuid?.removeHyphen(),
//                    "DKB_0AOC",
            bleDeviceId = result?.bleDeviceId,
            oneTimeKeyRequestToken = result?.oneTimeKeyRequestToken
        )
        val otk = CryptoGwDigitalKey(
            otkId = result?.publishId ?: "",
            grantId = result?.grantId ?: "",
            lockId = result?.lockId ?: "",
            bleDeviceId = result?.bleDeviceId ?: "",
            serviceUuid = result?.serviceUuid.removeHyphen() ?: "",
            useStartDate = result?.useStartDate ?: "",
            useEndDate = result?.useEndDate ?: "",
            slotId = result?.slotId ?: "",
            publishDate = result?.publishDate ?: ""
        )
        digitalKeyRepository.create(otk)
        return otk
    }

    /**
     *  サーバーから新しいワンタイム キーをダウンロードする
     *  @return DK関連処理結果クラス
     */
    suspend fun pullDigitalKey(): CryptoGwResult {
        var lockIdForLog = lockId
        val otksDownloaded = mutableListOf<CryptoGwDigitalKey>()
        try {
            if (!cryptoGwState.isInitialized) {
                throw CryptoGwNotInitializedException()
            }
            val localKeys = digitalKeyRepository.readAll()
            val localKeysMap = HashMap<String?, CryptoGwDigitalKey>()
            localKeys.forEach {
                localKeysMap[it.otkId] = it
            }
            val digitalKeyIds = localKeys.map { it.otkId }
            localKeys.firstOrNull { (it.grantId.isNotEmpty() && it.grantId == grantId) }
                ?.let {
                    lockIdForLog = it.lockId
                }
            val deletedCryptoGwDigitalKey = mutableListOf<CryptoGwDigitalKey>()
            val validatedDigitalKeyResponse = cryptoGWApiService.validateKeysByGrantOrLock(
                lockId = lockId,
                grantId = grantId,
                digitalKeyIds = digitalKeyIds
            )
            validatedDigitalKeyResponse?.validatedDigitalKeyResults?.forEach { validatedDigitalKeyResult ->
                if (validatedDigitalKeyResult.isDeleted) {
                    val localKey =
                        localKeysMap[validatedDigitalKeyResult.digitalKeyId] ?: return@forEach
                    deletedCryptoGwDigitalKey.add(localKey)
                }
            }
            digitalKeyDeleter.deleteOneTimeKeys(deletedCryptoGwDigitalKey)
            validatedDigitalKeyResponse?.digitalKeyIssuableGrants?.forEach { issuableGrants ->
                val grantIdDownloadKey = issuableGrants.grantId ?: return@forEach
                val oldKeys = digitalKeyRepository.readByGrantId(grantIdDownloadKey)
                digitalKeyDeleter.deleteOneTimeKeys(oldKeys)
                val otk = downloadKey(grantIdDownloadKey)
                otksDownloaded.add(otk)
            }
            otksDownloaded.firstOrNull()?.let { lockIdForLog = it.lockId }
            validatedDigitalKeyResponse?.error?.let {
                throw IssuableDigitalKeysLimitException()
            }

            logOperationRepository.create(
                sequenceName = sequenceName,
                functionName = "${sequenceName}.pullDigitalKeys",
                lockId = lockIdForLog,
                exception = null,
                oneTimeKeyId = otksDownloaded.map { it.otkId }.joinToStringCommas()
            )
            return CryptoGwResult(ResultType.SUCCESS)
        } catch (exception: IllegalStateException) {
            return CryptoGwResult.build(exception)
        } catch (exception: IllegalArgumentException) {
            return CryptoGwResult.build(exception)
        } catch (exception: Exception) {
            logOperationRepository.create(
                sequenceName = sequenceName,
                functionName = "${sequenceName}.pullDigitalKeys",
                lockId = lockIdForLog,
                exception = exception,
                oneTimeKeyId = otksDownloaded.map { it.otkId }.joinToStringCommas()
            )
            return CryptoGwResult.build(exception)
        } finally {
            logOperationRepository.sendToServer()
        }
    }

    /**
     * ハイフンを削除
     * @param? オリジナル文字列
     * @return 結果文字列
     */
    private fun String?.removeHyphen(): String? {
        return this?.replace("-", "")
    }

}