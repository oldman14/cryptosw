package jp.co.tokairika.cryptogw.entity.domain

internal class LogOperationInfo(
    var seqNo: Int = 1,
//    var status: Int = Status.SUCCESS,
    var lockId: String = "",
    var operationTime: String = "",
    var functionName: String = "",
    var sequenceName: String = "",
    var sdkId: String = "",
    var errorInfo: ErrorInfo? = null,
    var mobileInfo: MobileInfo? = null,
    var lockSerialNo: String = "",
    var userId: String = "",
    var oneTimeKeyId: String = "",
    var command: String = "",
    var dkbResponseData: String = ""
)

internal class ErrorInfo(
    var api: String = "",
    var errorInfoStatus: String = "",
    var libError: String = "",
    var sdkError: String = "",
    var errorResponseData: String = "",
    var logStatus : Int = Status.FAILED
)

internal class MobileInfo(
    var libVersion: String = "",
    var sdkVersion: String = "",
    var osType: String = "Android",
    var osVersion: String = "",
    var modelName: String = ""
)

object Status {
    const val SUCCESS = 0
    const val FAILED = 1
}

