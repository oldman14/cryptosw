package jp.co.tokairika.cryptogw.sdkaccessor

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.entity.domain.ValidatedKeyResult
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.manager.exception.InvalidDigitalKeyException
import jp.co.tokairika.cryptogw.manager.exception.NotExistDigitalKeyException
import jp.co.tokairika.cryptogw.manager.exception.OutOfTermDigitalKeyException
import jp.co.tokairika.cryptogw.usecase.accessor.ExecuteCommandUseCase
import jp.co.tokairika.cryptogw.utils.DateUtils
import jp.co.tokairika.cryptogw.utils.joinToStringCommas
import jp.co.tokairika.cryptogw.utils.takeIfEmpty
import jp.co.tokairika.cryptogw.utils.toHexString
import kotlinx.coroutines.withTimeout

internal class DigitalKeyValidator(
    private val sequenceName: String,
    private val dkCommands: List<DkCommand>,
    private val deleter: DigitalKeyDeleter,
    private val cryptoGwState: CryptoGwState,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository,
    private val cryptoGWApiService: CryptoGWApiService,
    private val grantId: String,
    private val lockId: String,
    private val executeBy: ExecuteCommandUseCase.ExecuteBy
) {

    /**
     * デジタルキーをインストールする
     *
     * @return ローカルワンタイムキー情報のリスト
     */
    suspend fun getAvailableOneTimeKeys(): List<CryptoGwDigitalKey> {
        val localOTKs = when (executeBy) {
            ExecuteCommandUseCase.ExecuteBy.Grant -> digitalKeyRepository.readByGrantId(grantId)
            ExecuteCommandUseCase.ExecuteBy.Lock -> digitalKeyRepository.readByLockId(lockId)
        }
        return validateKey(localOTKs = localOTKs, lockId = localOTKs.getOrNull(0)?.lockId.takeIfEmpty())
    }
    /**
     * ボックスに接続する前にキーを検証する
     * @param localOTKs ローカルワンタイムキー情報のリスト
     * @return 有効なローカル ワンタイム キー情報のリスト
     */
    private suspend fun validateKey(
        localOTKs: List<CryptoGwDigitalKey>,
        lockId: String = "",
    ): List<CryptoGwDigitalKey> {
        val result = if (cryptoGwState.isOnline) {
            try {
                withTimeout(3000) {
                    onlineValidateKey(
                        keys = localOTKs
                    )
                }
            } catch (e: Exception) {
                offlineValidateKey(localOTKs)
            }
        } else {
            offlineValidateKey(localOTKs)
        }
        val exception: Exception? = when {
            result.localKeys.isEmpty() -> NotExistDigitalKeyException()
            result.availableKeys.isEmpty() && result.deletedKeys.isNotEmpty() -> InvalidDigitalKeyException(
                ResultType.INVALID_DIGITAL_KEY.detail()
            )
            result.availableKeys.isEmpty() -> OutOfTermDigitalKeyException()
            else -> null
        }
        exception?.let {
            writeOperationLog(it, lockId, localOTKs)
            throw exception
        }
        return result.availableKeys
    }

    private fun writeOperationLog(exception: Exception, lockId: String = "", localOTKs: List<CryptoGwDigitalKey>) {
        logOperationRepository.create(
            functionName = "$sequenceName.validateKey",
            sequenceName = sequenceName,
            exception = exception,
            oneTimeKeyId = localOTKs.map { it.otkId }.joinToStringCommas(),
            command = dkCommands.map { it.controlCode.toHexString() }.joinToStringCommas(),
            lockId = lockId
        )
    }

    /**
     * ボックスに接続する前にキーを検証する
     * @param keys サーバーによって検証されるキーのリスト
     * @return 有効なワンタイム キー情報のリスト
     */
    private suspend fun onlineValidateKey(
        keys: List<CryptoGwDigitalKey>,
    ): ValidatedKeyResult {
        val availableOneTimeKeys = mutableListOf<CryptoGwDigitalKey>()
        val deletedOneTimeKeys = mutableListOf<CryptoGwDigitalKey>()
        val localKeysMap = HashMap<String?, CryptoGwDigitalKey>()
        if (keys.isEmpty()) {
            return ValidatedKeyResult(
                localKeys = keys,
                deletedKeys = deletedOneTimeKeys,
                availableKeys = availableOneTimeKeys
            )
        }
        keys.forEach {
            localKeysMap[it.otkId] = it
        }
        val digitalKeyIds = keys.map { it.otkId }
        val validatedDigitalKeyResponse = cryptoGWApiService.validateKeys(
            digitalKeyIds = digitalKeyIds
        )
        validatedDigitalKeyResponse?.validatedDigitalKeyResults?.forEach { validatedDigitalKeyResult ->
            val localKey = localKeysMap[validatedDigitalKeyResult.digitalKeyId] ?: return@forEach
            if (validatedDigitalKeyResult.isDeleted) {
                deletedOneTimeKeys.add(localKey)
            }
            if (validatedDigitalKeyResult.isValid) {
                availableOneTimeKeys.add(localKey)
            }
        }
        deleter.deleteOneTimeKeys(deletedOneTimeKeys)
        return ValidatedKeyResult(
            localKeys = keys,
            deletedKeys = deletedOneTimeKeys,
            availableKeys = availableOneTimeKeys
        )
    }

    /**
     * オフラインのワンタイム キーを検証する
     * @param localKeys ローカルワンタイムキー情報のリスト
     * @return 有効なワンタイム キー情報のリスト
     */
    private fun offlineValidateKey(localKeys: List<CryptoGwDigitalKey>): ValidatedKeyResult {
        val availableOneTimeKeys = mutableListOf<CryptoGwDigitalKey>()
        val deletedOneTimeKeys = mutableListOf<CryptoGwDigitalKey>()
        localKeys.forEach {
            val isValid = DateUtils.isNowBetween(it.useStartDate, it.useEndDate)
            val isExpire = DateUtils.isNowOver(it.useEndDate)
            if (isValid) {
                availableOneTimeKeys.add(it)
            }
            if (isExpire) {
                deletedOneTimeKeys.add(it)
            }
        }
        deleter.deleteOneTimeKeys(deletedOneTimeKeys)
        return ValidatedKeyResult(
            localKeys = localKeys,
            deletedKeys = deletedOneTimeKeys,
            availableKeys = availableOneTimeKeys
        )
    }
}
