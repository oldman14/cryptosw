package jp.co.tokairika.cryptogw.database.repository


import jp.co.tokairika.cryptogw.database.dao.DigitalKeyDao
import jp.co.tokairika.cryptogw.entity.db.CryptoGwDigitalKeyRealmModel
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey

/**
 * Repository use DAO to CRUD realm database
 */
internal class DigitalKeyRepository {

    /**
     * Data Access Object for OneTimeKeyInfoRealmModel
     */
    private val digitalKeyDao = DigitalKeyDao()


    /**
     * Create a record OneTimeKeyInfoRealmModel in realm database
     */
    suspend fun create(cryptoGwDigitalKey: CryptoGwDigitalKey) {
        digitalKeyDao.create(cryptoGwDigitalKey.toRealmModel())
    }

    /**
     * query all record OneTimeKeyInfoRealmModel in realm database
     */
    fun readAll(): List<CryptoGwDigitalKey> {
        return digitalKeyDao.readAll().map { it.toDomainModel() }
    }

    /**
     * query all record OneTimeKeyInfoRealmModel by grantId
     */
    fun readByGrantId(grantId: String): List<CryptoGwDigitalKey> {
        return digitalKeyDao.readByGrantId(grantId).map { it.toDomainModel() }
    }

    /**
     * query all record OneTimeKeyInfoRealmModel by lockId
     */
    fun readByLockId(lockId: String): List<CryptoGwDigitalKey> {
        return digitalKeyDao.readByLockId(lockId).map { it.toDomainModel() }
    }

    /**
     * delete record OneTimeKeyInfoRealmModel by otkId
     */
    fun deleteByOtkId(otkId: String) {
        digitalKeyDao.deleteByOtkId(otkId)
    }

    fun deleteAll() {
        digitalKeyDao.deleteAll()
    }

    /**
     * map OneTimeKeyInfo to OneTimeKeyInfoRealmModel
     */
    private fun CryptoGwDigitalKey.toRealmModel(): CryptoGwDigitalKeyRealmModel {
        return CryptoGwDigitalKeyRealmModel(
            otkId = otkId,
            grantId = grantId,
            lockId = lockId,
            bleDeviceId = bleDeviceId,
            serviceUuid = serviceUuid,
            useStartDate = useStartDate,
            useEndDate = useEndDate,
            slotId = slotId,
            publishDate = publishDate
        )
    }

    /**
     * map OneTimeKeyInfoRealmModel to OneTimeKeyInfo
     */
    private fun CryptoGwDigitalKeyRealmModel.toDomainModel(): CryptoGwDigitalKey {
        return CryptoGwDigitalKey(
            otkId = otkId,
            grantId = grantId,
            lockId = lockId,
            bleDeviceId = bleDeviceId,
            serviceUuid = serviceUuid,
            useStartDate = useStartDate,
            useEndDate = useEndDate,
            slotId = slotId,
            publishDate = publishDate
        )
    }

}