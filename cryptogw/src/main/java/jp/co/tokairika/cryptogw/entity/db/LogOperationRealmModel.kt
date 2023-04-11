package jp.co.tokairika.cryptogw.entity.db

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.co.tokairika.cryptogw.entity.domain.Status

open class LogOperationRealmModel : RealmObject {
    @PrimaryKey
    var seqNo: Int = 1
//    var status: Int = Status.SUCCESS
    var lockId: String = ""
    var sdkId: String = ""
    var operationTime: String = ""
    var functionName: String = ""
    var sequenceName: String = ""
    var errorInfo: ErrorInfoRealmModel? = null
    var mobileInfo: MobileInfoRealmModel? = null
    var lockSerialNo: String = ""
    var userId: String = ""
    var oneTimeKeyId: String = ""
    var command: String = ""
    var dkbResponseData: String = ""
}

open class ErrorInfoRealmModel : RealmObject {
    var api: String = ""
    var errorInfoStatus: String = ""
    var libError: String = ""
    var sdkError: String = ""
    var errorResponseData: String = ""
    var logStatus: Int = Status.SUCCESS
}

open class MobileInfoRealmModel : RealmObject {
    var libVersion: String = ""
    var sdkVersion: String = ""
    var osType: String = "Android"
    var osVersion: String = ""
    var modelName: String = ""
}
