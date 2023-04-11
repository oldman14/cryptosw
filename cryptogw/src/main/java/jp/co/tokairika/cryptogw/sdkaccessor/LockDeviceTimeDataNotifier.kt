package jp.co.tokairika.cryptogw.sdkaccessor

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.utils.takeIfEmpty
import jp.co.tokairika.cryptogw.utils.toHexString

internal class LockDeviceTimeDataNotifier(
    private val sequenceName: String,
    private val logOperationRepository: LogOperationRepository,
    private val cryptoGWApiService: CryptoGWApiService
) {


    /**
     * 時刻同期を実行する。
     */
    suspend fun syncTime(connection: LockDeviceConnection): Boolean {
        return try {
            val result = connection.getNotifyTimeData()
            val severTime = getSeverTime(connection, result)
            if (severTime.isNotEmpty()) {
                connection.notifyTime(severTime)
                return true
            }

            false
        } catch (exception: Exception) {
            false
        }
    }

    /**
     * サーバーから DK 時刻データを取得する
     * @param notifyTime DK からの文字列時間データ
     * @return DK に送信するためにサーバーから取得した新しい文字列の時刻データ
     */
    private suspend fun getSeverTime(connection: LockDeviceConnection, notifyTime: String): String {
        val digitalKey = connection.cryptoGwDigitalKey
        try {
            val serverTimeResult = cryptoGWApiService.getServerTime(
                digitalKey.otkId, notifyTime
            )
            return serverTimeResult?.notificationTimeData ?: ""
        } catch (exception: Exception) {
            logOperationRepository.create(
                functionName = "${sequenceName}.getSeverTime",
                sequenceName = sequenceName,
                lockId = digitalKey.lockId.takeIfEmpty(),
                exception = exception,
                oneTimeKeyId = digitalKey.otkId.takeIfEmpty(),
                command = connection.currentCommand?.controlCode.toHexString()
            )
            return ""
        }
    }
}