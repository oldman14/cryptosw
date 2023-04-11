package jp.co.tokairika.cryptogw.database.dao


import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import jp.co.tokairika.cryptogw.database.RealmConfigure
import jp.co.tokairika.cryptogw.entity.db.CryptoGwDigitalKeyRealmModel

/**
 * Data Access Object for OneTimeKeyInfoRealmModel
 */
internal class DigitalKeyDao {
    private val realm
        get() = RealmConfigure.getInstance()

    /**
     * Create a record OneTimeKeyInfoRealmModel in realm database
     */
    suspend fun create(cryptoGwDigitalKeyRealmModel: CryptoGwDigitalKeyRealmModel) {
        realm.write {
            this.copyToRealm(cryptoGwDigitalKeyRealmModel, UpdatePolicy.ALL)
        }
    }

    /**
     * query all record OneTimeKeyInfoRealmModel in realm database
     */
    fun readAll(): List<CryptoGwDigitalKeyRealmModel> {
        return realm.query<CryptoGwDigitalKeyRealmModel>().find()
    }

    /**
     * query all record OneTimeKeyInfoRealmModel by grantId
     */
    fun readByGrantId(grantId: String): List<CryptoGwDigitalKeyRealmModel> =
        realm.query<CryptoGwDigitalKeyRealmModel>(query = "${CryptoGwDigitalKeyRealmModel.GRANT_ID} == $0", grantId).find()

    /**
     * query all record OneTimeKeyInfoRealmModel by lockId
     */
    fun readByLockId(lockId: String): List<CryptoGwDigitalKeyRealmModel> =
        realm.query<CryptoGwDigitalKeyRealmModel>(query = "${CryptoGwDigitalKeyRealmModel.LOCK_ID} == $0", lockId).find()

    /**
     * delete record OneTimeKeyInfoRealmModel by otkId
     */
    fun deleteByOtkId(otkId: String) {
        realm.writeBlocking {
            val otk =
                this.query<CryptoGwDigitalKeyRealmModel>(query = "${CryptoGwDigitalKeyRealmModel.OTK_ID} == $0", otkId).first()
                    .find()
            otk?.let {
                delete(otk)
            }
        }
    }

    /**
     * delete all record OneTimeKeyInfoRealmModel by grantId
     */
    suspend fun deleteByGrantId(grantId: String) {
        realm.write {
            val otk =
                this.query<CryptoGwDigitalKeyRealmModel>(query = "${CryptoGwDigitalKeyRealmModel.GRANT_ID} == $0", grantId)
                    .find()
            delete(otk)
        }
    }

    /**
     * delete all record OneTimeKeyInfoRealmModel by lockId
     */
    suspend fun deleteByLockId(grantId: String) {
        realm.write {
            val otk =
                this.query<CryptoGwDigitalKeyRealmModel>(query = "${CryptoGwDigitalKeyRealmModel.GRANT_ID} == $0", grantId)
                    .find()
            delete(otk)
        }
    }

    fun deleteAll() {
        realm.writeBlocking {
            val allData = this.query<CryptoGwDigitalKeyRealmModel>().find()
            delete(allData)
        }
    }

}