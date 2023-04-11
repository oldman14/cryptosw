package jp.co.tokairika.cryptogw.database.dao

import io.realm.kotlin.ext.query
import io.realm.kotlin.query.max
import jp.co.tokairika.cryptogw.database.RealmConfigure
import jp.co.tokairika.cryptogw.entity.db.LogOperationRealmModel

class LogOperationDao {
    private val realm
        get() = RealmConfigure.getInstance()

    fun create(logOperationRealmModel: LogOperationRealmModel) {
        realm.writeBlocking {
            val currentMaxId = this.query<LogOperationRealmModel>().max<Int>("seqNo").find() ?: 0
            logOperationRealmModel.seqNo = currentMaxId + 1
            copyToRealm(logOperationRealmModel)
        }
    }

    fun readAll(): List<LogOperationRealmModel> {
        return realm.query<LogOperationRealmModel>().find()
    }

    fun deleteByIds(list: List<Int>) {
        realm.writeBlocking {
            list.forEach { seqNo ->
                val item: LogOperationRealmModel =
                    this.query<LogOperationRealmModel>("seqNo == $0", seqNo).find().first()
                delete(item)
            }
        }
    }

    fun deleteAll() {
        realm.writeBlocking {
            val data = this.query<LogOperationRealmModel>().find()
            delete(data)
        }
    }

}