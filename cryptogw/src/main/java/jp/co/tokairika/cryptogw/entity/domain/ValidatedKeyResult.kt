package jp.co.tokairika.cryptogw.entity.domain

/**
 * 有効なワンタイム キー情報
 */
internal class ValidatedKeyResult(
    val localKeys: List<CryptoGwDigitalKey>,
    val deletedKeys: List<CryptoGwDigitalKey>,
    val availableKeys: List<CryptoGwDigitalKey>
)
