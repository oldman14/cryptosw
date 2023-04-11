package jp.co.tokairika.cryptogw.sdkaccessor

import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptosdk.DkManager

internal class DigitalKeyDeleter(
    private val digitalKeyRepository: DigitalKeyRepository,
    private val dkManager: DkManager
) {

    fun deleteOneTimeKeys(keys: List<CryptoGwDigitalKey>) {
        if (keys.isEmpty()) return
        keys.forEach {
            deleteOneTimeKey(it)
        }
    }

    fun deleteOneTimeKey(otk: CryptoGwDigitalKey) {
        try {
            digitalKeyRepository.deleteByOtkId(otk.otkId)
            dkManager.deleteDkbAccessInfo(otk.otkId)
        } catch (e: Exception) {
            return
        }

    }

    /**
     * GRANT ID によるローカル ワンタイム キーの削除
     * @param grantId GRANT ID
     */
    fun removeAllKeysOfGrant(grantId: String) {
        val oldKeys = digitalKeyRepository.readByGrantId(grantId)
        deleteOneTimeKeys(keys = oldKeys)
    }

    /**
     * LOCK ID によるローカル ワンタイム キーの削除
     * @param lockId GRANT ID
     */
    fun removeAllKeysOfLock(lockId: String) {
        val oldKeys = digitalKeyRepository.readByLockId(lockId)
        deleteOneTimeKeys(keys = oldKeys)

    }

    /**
     * すべてのotkを削除
     */
    fun removeAllKeys() {
        val oldKeys = digitalKeyRepository.readAll()
        deleteOneTimeKeys(keys = oldKeys)
    }
}