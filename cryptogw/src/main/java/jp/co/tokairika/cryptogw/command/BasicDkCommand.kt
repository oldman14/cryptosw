package jp.co.tokairika.cryptogw.command

/**
 * 基本DKコマンド
 */
internal interface BasicDkCommand : DkCommand {
    companion object {
        /**
         * バイトデータの指定した位置のビット値を真偽値で返却
         *
         * @param value バイトデータ
         * @param position ビットの指定位置（0が最上位ビット、7が最下位ビット）
         * @return ビット値の真偽値
         */
        private fun isOn(value: Byte?, position: Int): Boolean {
            value ?: return false
            val tmpValue = value.toUInt()
            val mask = (0b10000000 ushr position).toUInt()
            return mask == (tmpValue and mask)
        }
    }

    override val needsNotifyTimeDataBeforeExecute: Boolean
        get() = false

    /**
     * ステータスコードをDKコマンド実行結果に変換する。
     * @param {ByteArray?} statusCode
     * @return {DkCommandResult} DKコマンド実行結果
     */
    override fun parseDkCommandResult(statusCode: ByteArray): DkCommandResult {
        val isSucceeded = statusCode.getOrNull(0)?.toInt().let {
            it == 0x00 || it == 0x01
        }

        val needsSendLog = statusCode.getOrNull(8)?.let {
            isOn(it, 2)
        } ?: false

        val needsNotifyTimeData = statusCode.getOrNull(8)?.let {
            isOn(it, 0) || isOn(it, 1)
        } ?: false

        return BasicDkCommandResult(
            isSucceeded = isSucceeded,
            statusCode = statusCode,
            needsSendDkLog = needsSendLog,
            needsNotifyTimeData = needsNotifyTimeData
        )
    }
}