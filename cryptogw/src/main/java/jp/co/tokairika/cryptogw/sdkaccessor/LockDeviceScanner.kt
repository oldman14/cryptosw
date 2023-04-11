package jp.co.tokairika.cryptogw.sdkaccessor

import android.bluetooth.le.ScanRecord
import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.utils.debounceSuspendCoroutine
import jp.co.tokairika.cryptogw.utils.joinToStringCommas
import jp.co.tokairika.cryptogw.utils.takeIfEmpty
import jp.co.tokairika.cryptogw.utils.toHexString
import jp.co.tokairika.cryptosdk.BleDevice
import jp.co.tokairika.cryptosdk.DkManager
import jp.co.tokairika.cryptosdk.DkbException
import jp.co.tokairika.cryptosdk.ScanMode
import jp.co.tokairika.cryptosdk.listener.BleScanListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class LockDeviceScanner(
    private val sequenceName: String,
    private val dkCommands: List<DkCommand>,
    private val logOperationRepository: LogOperationRepository,
    private val dkConnectOption: DkConnectOption,
) {
    private var otksValid = emptyList<CryptoGwDigitalKey>()
    private val dkbAccessor = DkManager.getDkbAccessor()

    /**
     * DKB接続処理を開始する。
     * @callBack
     */
    suspend fun startScan(digitalKeysValid: List<CryptoGwDigitalKey>): BleDevice {
        this.otksValid = digitalKeysValid
        val bleDeviceId = digitalKeysValid[0].bleDeviceId
        val serviceUuid = digitalKeysValid[0].serviceUuid
        return debounceSuspendCoroutine {
            val bleScanListener: BleScanListener = object : BleScanListener {
                override fun onScanDiscovered(
                    scanResult: BleDevice, rssi: Int, scanRecord: ScanRecord
                ) {
                    if (scanResult.bleDeviceId == bleDeviceId) {
                        stopScan()
                        it.resume(scanResult)
                    }
                }

                override fun onScanFailure(exception: DkbException) {
                    writeOperationLog(exception = exception, functionName = "startScan")
                    it.resumeWithException(exception)
                }
            }
            try {
                dkbAccessor.startScan(
                    duration = dkConnectOption.timeout.value,
                    serviceUuids = listOf(serviceUuid),
                    listener = bleScanListener,
                    scanMode = ScanMode.LOW_LATENCY
                )
            } catch (exception: Exception) {
                writeOperationLog(exception = exception, functionName = "startScan")
                it.resumeWithException(exception)
            }
        }
    }

    /**
     * DKB スキャン接続を停止します。
     */
    private fun stopScan() {
        try {
            dkbAccessor.stopScan()
        } catch (exception: Exception) {
            writeOperationLog(exception = exception, functionName = "stopScan")
            throw exception
        }
    }

    private fun writeOperationLog(
        exception: Exception? = null,
        functionName: String,
        dkbResponseData: String = ""
    ) {
        logOperationRepository.create(
            functionName = "${sequenceName}.$functionName",
            sequenceName = sequenceName,
            lockId = otksValid.firstOrNull()?.lockId.takeIfEmpty(),
            exception = exception,
            oneTimeKeyId = otksValid.map { it.otkId }.joinToStringCommas(),
            command = dkCommands.map { it.controlCode.toHexString() }.joinToStringCommas(),
            dkbResponseData = dkbResponseData
        )
    }

}