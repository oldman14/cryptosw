package jp.co.tokairika.cryptogw.command

/**
 * ロックデバイスの施錠/解錠の状態を反転させるDKコマンド
 */
class ReverseDkCommand : BasicDkCommand {
    override val controlCode: ByteArray = byteArrayOf(
        0x00,
        0x04,
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