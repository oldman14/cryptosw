package jp.co.tokairika.cryptogw.command

/**
 * ロックデバイスを解錠するDKコマンド
 */
class UnlockDkCommand : BasicDkCommand {
    override val controlCode: ByteArray = byteArrayOf(
        0x00,
        0x03,
        0x01,
        0x01,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )
    override val needsNotifyTimeDataBeforeExecute: Boolean
        get() = false
}