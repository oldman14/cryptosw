package jp.co.tokairika.cryptogw.manager.exception

import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptosdk.DkbException


internal class LockDeviceConnectFailedException(
    val cryptoGwDigitalKey: CryptoGwDigitalKey?,
    val dkbException: DkbException,
    val info: ByteArray? = null
) :
    Exception(dkbException)