package jp.co.tokairika.cryptogw.usecase.manager

import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptosdk.DkManager

internal class DeleteAllUseCase(
    private val dkManager: DkManager,
    private val logOperationRepository: LogOperationRepository,
    private val digitalKeyRepository: DigitalKeyRepository
) {

    /**
     * すべてのローカル データを削除する
     * @return DK関連処理結果クラス
     * */
    fun deleteAll() {
        dkManager.deleteAll()
        logOperationRepository.deleteAll()
        digitalKeyRepository.deleteAll()
        GwPreferenceUtils.clearAll()
    }
}