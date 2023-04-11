package jp.co.tokairika.cryptogw.sdkaccessor

import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.command.DkCommandResult
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.manager.exception.LockDeviceConnectFailedException
import jp.co.tokairika.cryptogw.utils.debounceSuspendCoroutine
import jp.co.tokairika.cryptogw.utils.joinToStringCommas
import jp.co.tokairika.cryptogw.utils.toHexString
import jp.co.tokairika.cryptosdk.BleDevice
import jp.co.tokairika.cryptosdk.DkManager
import jp.co.tokairika.cryptosdk.DkbException
import jp.co.tokairika.cryptosdk.listener.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class LockDeviceConnection(
    private val sequenceName: String,
    private val dkCommands: List<DkCommand>,
    val cryptoGwDigitalKey: CryptoGwDigitalKey,
    private val dkConnectOption: DkConnectOption,
    private val logOperationRepository: LogOperationRepository
) : DkbDisconnectListener,
    DkbErrorListener {
    private var handlerErrorBoxOrDisconnectError: (LockDeviceConnectFailedException) -> Unit = {}
    private var onDisconnectListener: (Exception?) -> Unit = {}

    private val dkbAccessor = DkManager.getDkbAccessor()

    var currentCommand: DkCommand? = null

    /**
     * Bluetooth経由でDKBに接続
     * @param bleDevice BLE接続クラス
     * @return ワンタイムキー情報
     */
    suspend fun connectBle(bleDevice: BleDevice?): LockDeviceConnection {
        try {
            return debounceSuspendCoroutine {
                handlerErrorBoxOrDisconnectError = { exception ->
                    writeOperationLog(
                        exception = exception,
                        functionName = "connectBle.handle",
                    )
                    it.resumeWithException(exception)
                }

                val dkbConnectionListener: DkbConnectionListener = object : DkbConnectionListener {
                    override fun onConnectionSuccess() {
                        it.resume(this@LockDeviceConnection)
                    }

                    override fun onConnectionFailure(exception: DkbException) {
                        writeOperationLog(exception = exception, functionName = "connectBle")
                        it.resumeWithException(exception)
                    }
                }
                try {
                    dkbAccessor.connectBle(
                        timeout = dkConnectOption.timeout.value,
                        oneTimeKeyId = cryptoGwDigitalKey.otkId,
                        bleDevice = bleDevice,
                        retryCount = DKB_CONNECT_RETRY_COUNT,
                        listener = dkbConnectionListener,
                        disconnectListener = this,
                        errorListener = this
                    )
                } catch (exception: Exception) {
                    writeOperationLog(exception = exception, functionName = "connectBle")
                    it.resumeWithException(exception)
                }

            }
        } finally {
            handlerErrorBoxOrDisconnectError = { }
        }
    }

    /**
     * Bluetooth経由でDKBに接続
     * @param dkCommand  DKコマンド
     * @return 成功したかどうかのみを保持する DK コマンドの結果
     */
    suspend fun control(dkCommand: DkCommand): DkCommandResult {
        currentCommand = dkCommand
        try {
            return debounceSuspendCoroutine {
                handlerErrorBoxOrDisconnectError = { exception ->
                    writeOperationLog(
                        exception = exception,
                        functionName = "control.handle",
                    )
                    it.resumeWithException(exception)
                }

                val dkControlListener = object : DkbControlListener {
                    override fun onControlSuccess(statusCode: ByteArray) {
                        writeOperationLog(
                            exception = null,
                            functionName = "control",
                            dkbResponseData = statusCode.toHexString()
                        )
                        it.resume(dkCommand.parseDkCommandResult(statusCode))
                    }

                    override fun onControlFailure(exception: DkbException) {
                        writeOperationLog(
                            exception = exception,
                            functionName = "control"
                        )
                        it.resumeWithException(exception)
                    }
                }
                try {
                    dkbAccessor.control(
                        timeout = DkConnectOption.Timeout.THIRTY.value,
                        controlCode = dkCommand.controlCode,
                        listener = dkControlListener
                    )
                } catch (exception: Exception) {
                    writeOperationLog(exception = exception, functionName = "control")
                    it.resumeWithException(exception)
                }

            }
        } finally {
            handlerErrorBoxOrDisconnectError = {}
        }
    }

    /**
     * 時刻通知情報を取得する。
     */
    suspend fun getNotifyTimeData(): String {
        try {
            return debounceSuspendCoroutine {
                handlerErrorBoxOrDisconnectError = { exception ->
                    writeOperationLog(
                        exception = exception,
                        functionName = "getNotifyTimeData.handle",
                    )
                    it.resumeWithException(exception)
                }
                val listener = object : DkbGetNotificationTimeDataListener {
                    override fun onGetNotificationTimeDataSuccess(requestTimeData: String) {
                        it.resume(requestTimeData)
                    }

                    override fun onGetTimeDataFailure(exception: DkbException) {
                        writeOperationLog(
                            exception = exception,
                            functionName = "getNotifyTimeData"
                        )
                        it.resumeWithException(exception)
                    }
                }

                try {
                    dkbAccessor.getNotificationTimeData(
                        timeout = DkConnectOption.Timeout.FIVE.value, listener = listener
                    )
                } catch (exception: Exception) {
                    writeOperationLog(
                        exception = exception,
                        functionName = "getNotifyTimeData"
                    )
                    it.resumeWithException(exception)
                }
            }
        } finally {
            handlerErrorBoxOrDisconnectError = {}
        }
    }

    /**
     * 時刻通知処理を実行する。
     * @param notificationTimeData 時刻通知データ
     */
    suspend fun notifyTime(notificationTimeData: String?) {
        try {
            return debounceSuspendCoroutine {
                handlerErrorBoxOrDisconnectError = { exception ->
                    writeOperationLog(
                        exception = exception,
                        functionName = "notifyTime.handle",
                    )
                    it.resumeWithException(exception)
                }
                val dkbNotifyTimeListener = object : DkbNotifyTimeListener {
                    override fun onNotifyTimeFailure(exception: DkbException) {
                        writeOperationLog(
                            functionName = "notifyTime",
                            exception = exception
                        )
                        it.resumeWithException(exception)
                    }

                    override fun onNotifyTimeSuccess() {
                        it.resume(Unit)
                    }
                }
                try {
                    dkbAccessor.notifyTime(notificationTimeData, dkbNotifyTimeListener)
                } catch (exception: Exception) {
                    writeOperationLog(
                        functionName = "notifyTime",
                        exception = exception
                    )
                    it.resumeWithException(exception)
                }

            }
        } finally {
            handlerErrorBoxOrDisconnectError = {}
        }
    }

    /**
     * DKB を切断する
     */
    suspend fun disconnect() {
        try {
            withTimeoutOrNull(500L) {
                suspendCancellableCoroutine { continuation ->
                    onDisconnectListener = { exception ->
                        exception?.let {
                            writeOperationLog(it, "disconnect")
                        }
                        continuation.resume(Unit)
                    }
                    dkbAccessor.disconnect()
                }
            }
        } finally {
            onDisconnectListener = {}
        }
    }


    override fun onErrorReceive(exception: DkbException, info: ByteArray?) {
        val connectFailedException =
            LockDeviceConnectFailedException(cryptoGwDigitalKey, exception, info)
        handlerErrorBoxOrDisconnectError(connectFailedException)
    }

    override fun onDisconnectSuccess() {
        onDisconnectListener(null)
    }

    override fun onDisconnectFailure(exception: DkbException) {
        val connectFailedException = LockDeviceConnectFailedException(cryptoGwDigitalKey, exception)
        handlerErrorBoxOrDisconnectError(connectFailedException)
        onDisconnectListener(connectFailedException)
    }

    private fun writeOperationLog(
        exception: Exception? = null,
        functionName: String,
        dkbResponseData: String = ""
    ) {
        val commandString = currentCommand?.controlCode?.toHexString() ?: dkCommands.map { it.controlCode.toHexString() }
            .joinToStringCommas()

        logOperationRepository.create(
            functionName = "${sequenceName}.$functionName",
            sequenceName = sequenceName,
            lockId = cryptoGwDigitalKey.lockId,
            exception = exception,
            oneTimeKeyId = cryptoGwDigitalKey.otkId,
            command = commandString,
            dkbResponseData = dkbResponseData
        )
    }

    private companion object {
        const val DKB_CONNECT_RETRY_COUNT = 2
    }
}