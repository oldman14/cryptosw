package jp.co.tokairika.cryptogw.command

/**
 * ロックデバイスの状態取得DKコマンド
 */
class StatusDkCommand : BasicDkCommand {
    override val controlCode: ByteArray = byteArrayOf(
        0x00,
        0x01,
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