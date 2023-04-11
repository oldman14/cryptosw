package jp.co.tokairika.cryptogw.command

/**
 * ロックデバイスを施錠するDKコマンド
 */
class LockDkCommand : BasicDkCommand {
    override val controlCode: ByteArray = byteArrayOf(
        0x00,
        0x02,
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