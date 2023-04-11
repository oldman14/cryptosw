package jp.co.tokairika.cryptogw.sdkaccessor

import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.manager.exception.InvalidDigitalKeyException
import jp.co.tokairika.cryptosdk.BleDevice

internal class LockDeviceConnector(
    private val digitalKeyDeleter: DigitalKeyDeleter,
    private val sequenceName: String,
    private val dkConnectOption: DkConnectOption,
    private val logOperationRepository: LogOperationRepository
) {

    suspend fun connect(
        keys: List<CryptoGwDigitalKey>,
        dkCommands: List<DkCommand>,
        bleDevice: BleDevice?,
    ): LockDeviceConnection {
        return tryConnect(keys, bleDevice, dkCommands)
    }

    private suspend fun tryConnect(
        cryptoGwDigitalKeys: List<CryptoGwDigitalKey>,
        bleDevice: BleDevice?,
        dkCommands: List<DkCommand>
    ): LockDeviceConnection {
        val iterator = cryptoGwDigitalKeys.iterator()
        while (iterator.hasNext()) {
            val digitalKey = iterator.next()
            val lockDeviceConnection =
                LockDeviceConnection(sequenceName, dkCommands, digitalKey, dkConnectOption, logOperationRepository)
            try {
                return lockDeviceConnection.connectBle(bleDevice)
            } catch (exception: Exception) {
                lockDeviceConnection.disconnect()
                val resultType = ResultType.from(exception)
                val isInvalidKey = resultType == ResultType.INVALID_DIGITAL_KEY
                if (isInvalidKey) {
                    digitalKeyDeleter.deleteOneTimeKey(digitalKey)
                    if (iterator.hasNext()) {
                        continue
                    }
                }
                throw exception
            }
        }
        throw InvalidDigitalKeyException(ResultType.INVALID_DIGITAL_KEY.detail())
    }
}