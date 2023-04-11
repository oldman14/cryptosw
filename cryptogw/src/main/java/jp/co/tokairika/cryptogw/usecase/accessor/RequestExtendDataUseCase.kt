package jp.co.tokairika.cryptogw.usecase.accessor

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.entity.domain.ExtendData
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWCallback
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWDataReceiveCallback
import jp.co.tokairika.cryptogw.utils.hexToByteArrays
import jp.co.tokairika.cryptogw.utils.toHexString
import jp.co.tokairika.cryptosdk.DkManager
import jp.co.tokairika.cryptosdk.DkbException
import jp.co.tokairika.cryptosdk.listener.DkbExtendedDataReceiveListener
import jp.co.tokairika.cryptosdk.listener.DkbExtendedServiceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


internal class RequestExtendDataUseCase(
    private val dkManager: DkManager,
    private val cryptoGWApiService: CryptoGWApiService,
) {
    var extendData: ExtendData? = null

    /**
     * 延長サービスを申し込む
     * @param callback DK初期化処理のインターフェース
     * @param dataReceiveCallback CryptoGW データ受信インターフェース
     * @param cryptoGWCoroutineScope CryptoGW コルーチンのスコープ
     */
    fun requestExtendedService(
        callback: CryptoGWCallback,
        dataReceiveCallback: CryptoGWDataReceiveCallback,
        cryptoGWCoroutineScope: CoroutineScope
    ) {
        val listener: DkbExtendedServiceListener = object : DkbExtendedServiceListener {
            override fun onExtendedServiceRequestFailure(exception: DkbException) {
                throw exception
            }

            override fun onExtendedServiceRequestSuccess(
                payloadSize: Int,
                continuousFrameCount: Int
            ) {

            }
        }
        val dataReceiveListener: DkbExtendedDataReceiveListener =
            object : DkbExtendedDataReceiveListener {
                override fun onDataReceive(header: ByteArray, data: ByteArray) {
                    cryptoGWCoroutineScope.launch {
                        val dataReceive = handleDataReceive(data)
                        withContext(Dispatchers.Main) {
                            dataReceiveCallback.onDataReceive(dataReceive)
                        }
                    }
                }

                override fun onDataReceiveFailure(exception: DkbException) {
                    throw exception
                }

            }
        try {
            dkManager.getDkbAccessor().requestExtendedService(
                REQUEST_TIMEOUT,
                EXTENDED_DKB_SERVICE_UUID,
                EXTENDED_DKB_WRITE_REQUEST_UUID,
                EXTENDED_DKB_WRITE_COMMAND_UUID,
                EXTENDED_DKB_INDICATION_UUID,
                EXTENDED_DKB_NOTIFICATION_UUID,
                DKB_MTU_SIZE,
                listener,
                dataReceiveListener
            )
        } catch (exception: DkbException) {

        } catch (exception: Exception) {

        }
    }

    /**
     * 受信データの扱い
     * @param data {ByteArray?} 結果コード。 DK から返されたバイト値
     * @return String
     */
    private suspend fun handleDataReceive(data: ByteArray): String {
        if (extendData == null) return ""
        extendData?.byteDataReceive = data.toHexString()
        var dataReceive = extendData?.byteDataReceive
        if (extendData?.isServerEncrypted == true) {
            dataReceive = decryptDataBySever(extendData)
        } else extendData?.byteDataReceive
        return dataReceive ?: ""
    }

    /**
     * サーバーによるデータの復号化
     * @param extendData DKB 拡張データ
     * @return String
     */
    private suspend fun decryptDataBySever(extendData: ExtendData?): String {
        if (extendData == null) return ""
        val dataReceiveWithPadding = extendData.byteDataReceive.hexToByteArrays()
        val dataReceive = ByteArray(dataReceiveWithPadding.size - extendData.bytePadding) {
            dataReceiveWithPadding[it]
        }
        val result = cryptoGWApiService.getSeverDecryptData(
            dataReceive.toHexString(),
            extendData.dkbSerialNo
        )
        return result?.decryptionData ?: ""
    }

    private companion object {
        const val REQUEST_TIMEOUT = 30000
        const val DKB_MTU_SIZE = 512
        const val EXTENDED_DKB_SERVICE_UUID = "66ae5fd0a42511ebbcbc0242ac13ffc0"
        const val EXTENDED_DKB_WRITE_COMMAND_UUID = "66ae5fd1a42511ebbcbc0242ac13ffc0"
        const val EXTENDED_DKB_WRITE_REQUEST_UUID = "66ae5fd2a42511ebbcbc0242ac13ffc0"
        const val EXTENDED_DKB_INDICATION_UUID = "66ae5fd3a42511ebbcbc0242ac13ffc0"
        const val EXTENDED_DKB_NOTIFICATION_UUID = "66ae5fd4a42511ebbcbc0242ac13ffc0"
    }
}