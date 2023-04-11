package jp.co.tokairika.cryptogw.database.repository

import android.os.Build
import jp.co.tokairika.cryptogw.BuildConfig
import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.dao.LogOperationDao
import jp.co.tokairika.cryptogw.entity.db.ErrorInfoRealmModel
import jp.co.tokairika.cryptogw.entity.db.LogOperationRealmModel
import jp.co.tokairika.cryptogw.entity.db.MobileInfoRealmModel
import jp.co.tokairika.cryptogw.entity.domain.ErrorInfo
import jp.co.tokairika.cryptogw.entity.domain.LogOperationInfo
import jp.co.tokairika.cryptogw.entity.domain.MobileInfo
import jp.co.tokairika.cryptogw.entity.domain.Status
import jp.co.tokairika.cryptogw.manager.CryptoGwResult
import jp.co.tokairika.cryptogw.manager.exception.DkCommandFailedException
import jp.co.tokairika.cryptogw.manager.exception.LockDeviceConnectFailedException
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.security.GwKeyStore
import jp.co.tokairika.cryptogw.utils.DateUtils
import jp.co.tokairika.cryptogw.utils.takeIfEmpty
import jp.co.tokairika.cryptogw.utils.toHexString
import jp.co.tokairika.cryptosdk.DkManager
import jp.co.tokairika.cryptosdk.DkbException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

internal class LogOperationRepository {
    private val logOperationDao = LogOperationDao()
    private val cryptoGWApiService = CryptoGWApiService()

    private val sdkVersionName = DkManager.getVersionName()
    private val gwVersionName = BuildConfig.GW_VERSION

    /** 操作ログ送信非同期処理用 */
    private val sendOperationLogCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()

    fun create(
        functionName: String,
        lockId: String? = "",
        exception: Exception?,
        sequenceName: String,
        oneTimeKeyId: String = "",
        command: String = "",
        dkbResponseData: String = ""
    ) {
        return try {
            if (exception is HttpException && exception.code() == 401) return
            val logOperationInfo = LogOperationInfo(
                functionName = functionName,
                sdkId = GwPreferenceUtils.sdkId,
                errorInfo = getErrorInfo(
                    exception = exception
                ),
                mobileInfo = getMobileInfo(),
                operationTime = DateUtils.nowLogOperation(),
                lockId = lockId.takeIfEmpty(),
                sequenceName = sequenceName,
                oneTimeKeyId = oneTimeKeyId,
                command = command,
                dkbResponseData = dkbResponseData
            )
            logOperationDao.create(logOperationInfo.toRealmModel())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * sendToServer
     */
    suspend fun sendToServer() {
        sendOperationLogCoroutineScope.launch {
            mutex.withLock {
                try {
                    val models = logOperationDao.readAll()
                    val entities = models.map { it.toDomainModel() }
                    if (entities.isEmpty()) {
                        return@launch
                    }
                    cryptoGWApiService.logService(logServiceRequest = entities)
                    logOperationDao.deleteByIds(models.map { it.seqNo })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun deleteAll() {
        logOperationDao.deleteAll()
    }

    private fun getErrorInfo(
        exception: Exception?,
    ): ErrorInfo {
        val logStatus = if (exception == null) Status.SUCCESS else Status.FAILED
        if (exception == null) {
            return ErrorInfo(libError = "", logStatus = logStatus)
        }
        val gwResult = CryptoGwResult.build(exception)
        val libError = "${gwResult.resultType.code}: ${gwResult.message.replace("\n", " ")}"

        return when (exception) {
            is HttpException -> {
                val endPoint = exception.response()?.raw()?.request?.url?.encodedPath.takeIfEmpty()
                ErrorInfo(
                    api = endPoint,
                    errorInfoStatus = exception.code().toString(),
                    libError = libError,
                    logStatus = logStatus
                )
            }

            is LockDeviceConnectFailedException -> {
                val dkbException = exception.dkbException
                val sdkError = "${dkbException.errorCode} ${dkbException.errorMessage}"
                ErrorInfo(
                    errorResponseData = exception.info.toHexString(),
                    sdkError = sdkError,
                    libError = libError,
                    logStatus = logStatus
                )
            }

            is DkbException -> {
                val sdkError = "${exception.errorCode} ${exception.errorMessage}"
                ErrorInfo(sdkError = sdkError, libError = libError, logStatus = logStatus)
            }

            is DkCommandFailedException -> {
                ErrorInfo(sdkError = "", libError = libError, logStatus = logStatus)
            }

            else -> {
                ErrorInfo(libError = libError, logStatus = logStatus)
            }
        }
    }

    private fun getMobileInfo(): MobileInfo {
        return MobileInfo(
            libVersion = gwVersionName,
            sdkVersion = sdkVersionName,
            osType = "Android",
            osVersion = Build.VERSION.RELEASE.toString(),
            modelName = Build.MODEL,
        )
    }

    private fun LogOperationInfo.toRealmModel(): LogOperationRealmModel {
        return LogOperationRealmModel().apply {
            lockId = this@toRealmModel.lockId
            sdkId = this@toRealmModel.sdkId
            operationTime = this@toRealmModel.operationTime
            functionName = this@toRealmModel.functionName
            sequenceName = this@toRealmModel.sequenceName
            errorInfo = this@toRealmModel.errorInfo?.toRealmModel()
            mobileInfo = this@toRealmModel.mobileInfo?.toRealmModel()
            lockSerialNo = this@toRealmModel.lockSerialNo
            userId = this@toRealmModel.userId
            oneTimeKeyId = this@toRealmModel.oneTimeKeyId
            command = this@toRealmModel.command
            dkbResponseData = this@toRealmModel.dkbResponseData
        }
    }

    private fun ErrorInfo.toRealmModel(): ErrorInfoRealmModel {
        return ErrorInfoRealmModel().apply {
            api = this@toRealmModel.api.takeIfEmpty()
            errorInfoStatus = this@toRealmModel.errorInfoStatus
            libError = this@toRealmModel.libError.takeIfEmpty()
            sdkError = this@toRealmModel.sdkError.takeIfEmpty()
            errorResponseData = this@toRealmModel.errorResponseData.takeIfEmpty()
            logStatus = this@toRealmModel.logStatus
        }
    }

    private fun MobileInfo.toRealmModel(): MobileInfoRealmModel {
        return MobileInfoRealmModel().apply {
            libVersion = this@toRealmModel.libVersion
            sdkVersion = this@toRealmModel.sdkVersion
            osType = this@toRealmModel.osType
            osVersion = this@toRealmModel.osVersion
            modelName = this@toRealmModel.modelName
        }
    }

    private fun LogOperationRealmModel.toDomainModel(): LogOperationInfo {
        return LogOperationInfo().apply {
            seqNo = this@toDomainModel.seqNo
            lockId = this@toDomainModel.lockId
            sdkId = this@toDomainModel.sdkId
            operationTime = this@toDomainModel.operationTime
            functionName = this@toDomainModel.functionName
            sequenceName = this@toDomainModel.sequenceName
            errorInfo = this@toDomainModel.errorInfo?.toDomainModel()
            mobileInfo = this@toDomainModel.mobileInfo?.toDomainModel()
            lockSerialNo = this@toDomainModel.lockSerialNo
            userId = this@toDomainModel.userId
            oneTimeKeyId = this@toDomainModel.oneTimeKeyId
            command = this@toDomainModel.command
            dkbResponseData = this@toDomainModel.dkbResponseData
        }
    }

    private fun ErrorInfoRealmModel.toDomainModel(): ErrorInfo {
        return ErrorInfo().apply {
            api = this@toDomainModel.api
            errorInfoStatus = this@toDomainModel.errorInfoStatus
            libError = this@toDomainModel.libError
            sdkError = this@toDomainModel.sdkError
            errorResponseData = this@toDomainModel.errorResponseData
            logStatus = this@toDomainModel.logStatus
        }
    }

    private fun MobileInfoRealmModel.toDomainModel(): MobileInfo {
        return MobileInfo().apply {
            libVersion = this@toDomainModel.libVersion
            sdkVersion = this@toDomainModel.sdkVersion
            osType = this@toDomainModel.osType
            osVersion = this@toDomainModel.osVersion
            modelName = this@toDomainModel.modelName
        }
    }
}