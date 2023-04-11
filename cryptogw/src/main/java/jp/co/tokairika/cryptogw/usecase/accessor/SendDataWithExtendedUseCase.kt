package jp.co.tokairika.cryptogw.usecase.accessor

import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.entity.domain.ExtendData
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWCallback
import jp.co.tokairika.cryptogw.utils.hexToByteArrays
import jp.co.tokairika.cryptogw.utils.toHexString
import jp.co.tokairika.cryptosdk.BleWriteType
import jp.co.tokairika.cryptosdk.DkManager
import jp.co.tokairika.cryptosdk.DkbException
import jp.co.tokairika.cryptosdk.listener.DkbExtendedDataSendListener


internal class SendDataWithExtendedUseCase(
    private val dkManager: DkManager,
    private val cryptoGWApiService: CryptoGWApiService,
) {

    /**
     * 拡張されたデータを送信
     * @param header {ByteArray?} ヘッダー . バイト値
     * @param data {ByteArray} データ . DKに送信されるバイト値
     * @param dkbSerialNo  DKシリアルナンバー
     * @param callback DK初期化処理のインターフェース
     * @param requestExtendDataUseCase  使用事例
     */
    suspend fun sendDataWithExtended(
        header: ByteArray?,
        data: ByteArray,
        dkbSerialNo: String?,
        callback: CryptoGWCallback,
        requestExtendDataUseCase: RequestExtendDataUseCase?
    ) {
        val listener: DkbExtendedDataSendListener = object : DkbExtendedDataSendListener {
            override fun onDataSendFailure(exception: DkbException) {
                throw exception
            }

            override fun onDataSendSuccess() {

            }

        }
        try {
            var dataSendToBox = data
            requestExtendDataUseCase?.extendData = ExtendData(
                dkbSerialNo = dkbSerialNo ?: "",
                byteDataSend = data.toHexString(),
                header = header,
                isServerEncrypted = isSeverEncrypt(header)
            )
            if (isSeverEncrypt(header) && dkbSerialNo != null) {
                dataSendToBox = encryptDataByServer(data, dkbSerialNo)
            }
            dkManager.getDkbAccessor()
                .sendDataWithExtended(
                    SEND_TIMEOUT,
                    header,
                    dataSendToBox,
                    getWriteType(header),
                    listener
                )
        } catch (exception: DkbException) {


        } catch (exception: Exception) {

        }
    }

    /**
     * サーバーによる暗号化データ
     * @param data {ByteArray} データ。 暗号化のためにサーバーに送信されるバイト値
     * @param dkbSerialNo DKシリアルナンバー
     * @return {ByteArray} 結果データ。 サーバーから受信したバイト値
     */
    private suspend fun encryptDataByServer(data: ByteArray, dkbSerialNo: String): ByteArray {
        val result = cryptoGWApiService.getServerEncryptData(data.toHexString(), dkbSerialNo)
        val encryptionData = result?.encryptionData.hexToByteArrays()
        var dataWithPadding = encryptionData.clone()
        val bytePadding = 16 - encryptionData.size % 16
        if (bytePadding != 16) {
            dataWithPadding = ByteArray(encryptionData.size + bytePadding) {
                if (it < encryptionData.size) encryptionData[it] else 0x00
            }
        }
        return dataWithPadding
    }

    /**
     * サーバー暗号化の確認
     * @param header {ByteArray} ヘッダー データ。 バイト値
     * @return 正しいか間違っているか
     */
    private fun isSeverEncrypt(header: ByteArray?): Boolean {
        val headerHexString = header.toHexString()
        return headerHexString == EXTENDED_HEADER_SEVER_ENCRYPT_REQUEST_COMMAND
                || headerHexString == EXTENDED_HEADER_SEVER_ENCRYPT_WRITE_COMMAND
                || headerHexString == EXTENDED_HEADER_BOTH_ENCRYPT_REQUEST_COMMAND
                || headerHexString == EXTENDED_HEADER_BOTH_ENCRYPT_WRITE_COMMAND
    }

    /**
     * BLE 書き込みタイプの取得
     * @param header {ByteArray} ヘッダー データ。 バイト値
     * @return BLE書き込み種別
     */
    private fun getWriteType(header: ByteArray?): BleWriteType {
        return when (header.toHexString()) {
            EXTENDED_HEADER_SEVER_ENCRYPT_WRITE_COMMAND,
            EXTENDED_HEADER_NO_ENCRYPT_WRITE_COMMAND,
            EXTENDED_HEADER_MOBILE_ENCRYPT_WRITE_COMMAND,
            EXTENDED_HEADER_BOTH_ENCRYPT_WRITE_COMMAND -> BleWriteType.WRITE_COMMAND
            else -> BleWriteType.WRITE_REQUEST
        }
    }


    companion object {
        private const val SEND_TIMEOUT = 30000
        const val EXTENDED_HEADER_NO_ENCRYPT_REQUEST_COMMAND = "00000FF0"
        const val EXTENDED_HEADER_NO_ENCRYPT_WRITE_COMMAND = "00000FF1"

        const val EXTENDED_HEADER_MOBILE_ENCRYPT_REQUEST_COMMAND = "80000FF0"
        const val EXTENDED_HEADER_MOBILE_ENCRYPT_WRITE_COMMAND = "80000FF1"

        const val EXTENDED_HEADER_SEVER_ENCRYPT_REQUEST_COMMAND = "40000FF0"
        const val EXTENDED_HEADER_SEVER_ENCRYPT_WRITE_COMMAND = "40000FF1"

        const val EXTENDED_HEADER_BOTH_ENCRYPT_REQUEST_COMMAND = "C0000FF0"
        const val EXTENDED_HEADER_BOTH_ENCRYPT_WRITE_COMMAND = "C0000FF1"
    }
}