package jp.co.tokairika.cryptogw.database

import android.util.Base64
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import jp.co.tokairika.cryptogw.entity.db.ErrorInfoRealmModel
import jp.co.tokairika.cryptogw.entity.db.LogOperationRealmModel
import jp.co.tokairika.cryptogw.entity.db.MobileInfoRealmModel
import jp.co.tokairika.cryptogw.entity.db.CryptoGwDigitalKeyRealmModel
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import java.security.SecureRandom


/**
 * Realm関連処理用
 */
internal object RealmConfigure {

    // RealmのDBバージョン
    private var realm: Realm? = null

    private const val DEFAULT_DB_VERSION = 1L
    private const val DATABASE_NAME = "cryptoGW.realm"

    /**
     * get config for realm
     */
    private val config: RealmConfiguration
        get()  {
            return RealmConfiguration.Builder(
                setOf(
                    CryptoGwDigitalKeyRealmModel::class,
                    LogOperationRealmModel::class,
                    ErrorInfoRealmModel::class,
                    MobileInfoRealmModel::class
                )
            )
                .name(DATABASE_NAME)
                .schemaVersion(DEFAULT_DB_VERSION)
                .encryptionKey(getDBKey())
//        .migration(Migration())
                .deleteRealmIfMigrationNeeded()
                .build()

        }



    /**
     * initialize realm
     */
    fun init() {
        if (realm == null) {
            realm = Realm.open(config)
        }
    }

    /**
     * delete all data and file of realm
     */
    fun delete() {
        realm?.close()
        Realm.deleteRealm(config)
        realm = null
    }

    /**
     * Realmオブジェクトをインスタンス化する
     * 作成したスレッドに依存してしまうので、使用時に随時取得する
     *
     * @return Realm
     */
    fun getInstance(): Realm {
        if (realm == null) {
            init()
        }
        return realm!!
    }

    /**
     * get encrypt key of realm database
     */
    private fun getDBKey(): ByteArray {
        val databaseKey = GwPreferenceUtils.databaseKey
        if (databaseKey.isEmpty()) {
            return createDBKey()
        }
        return Base64.decode(databaseKey, Base64.DEFAULT)
    }

    /**
     * create encrypt key of realm database
     */
    private fun createDBKey(): ByteArray {
        val encryptionKey = ByteArray(64)
        SecureRandom().nextBytes(encryptionKey)
        GwPreferenceUtils.databaseKey = Base64.encodeToString(encryptionKey, Base64.NO_WRAP)
        return encryptionKey
    }
}