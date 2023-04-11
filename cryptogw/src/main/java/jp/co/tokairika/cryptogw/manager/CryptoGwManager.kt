package jp.co.tokairika.cryptogw.manager

import android.content.Context
import jp.co.tokairika.cryptogw.command.DkCommand
import jp.co.tokairika.cryptogw.entity.domain.CryptoGwDigitalKey
import jp.co.tokairika.cryptogw.manager.callback.CryptoGWCallback
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandCallback
import jp.co.tokairika.cryptogw.manager.callback.ExecuteDkCommandsCallback

interface CryptoGwManager {

    /**
     * 初期処理
     * @param callback DK初期化処理のインターフェース
     */
    fun initialize(
        callback: CryptoGWCallback
    )

    /**
     * デジタルキーをインストールする
     *
     * @param grantId GRANT ID
     * @param callback DK初期化処理のインターフェース
     */
    fun pullDigitalKeysByGrant(
        grantId: String,
        callback: CryptoGWCallback
    )

    /**
     * デジタルキーをインストールする
     *
     * @param lockId LOCK ID
     * @param callback DK初期化処理のインターフェース
     */
    fun pullDigitalKeysByLock(
        lockId: String,
        callback: CryptoGWCallback
    )

    /**
     * DKコマンドを実行する。
     *
     * @param grantId GRANT ID
     * @param dkCommand DKコマンド
     * @param callback DKコマンドの実行結果を取得するコールバック
     */
    fun executeDkCommandByGrant(
        grantId: String,
        dkCommand: DkCommand,
        callback: ExecuteDkCommandCallback
    )

    /**
     * DKコマンドを実行する。
     *
     * @param lockId ロックID
     * @param dkCommand DKコマンド
     * @param callback DKコマンドの実行結果を取得するコールバック
     */
    fun executeDkCommandByLock(
        lockId: String,
        dkCommand: DkCommand,
        callback: ExecuteDkCommandCallback
    )

    /**
     * DKコマンドリストを実行する。
     *
     * @param grantId GRANT ID
     * @param dkCommands DKコマンドリスト
     * @param callback DKコマンドの実行結果を取得するコールバック
     */
    fun executeDkCommandsByGrant(
        grantId: String,
        dkCommands: List<DkCommand>,
        callback: ExecuteDkCommandsCallback
    )

    /**
     * DKコマンドリストを実行する。
     *
     * @param lockId ロックID
     * @param dkCommands DKコマンドリスト
     * @param callback DKコマンドの実行結果を取得するコールバック
     */
    fun executeDkCommandsByLock(
        lockId: String,
        dkCommands: List<DkCommand>,
        callback: ExecuteDkCommandsCallback
    )

    /**
     * すべてのotkを取得
     * @param grantId ロックID
     * @return ワンタイムキーのリスト
     */
    fun getDigitalKeysByGrantId(grantId: String): List<CryptoGwDigitalKey>

    /**
     * すべてのotkを取得
     * @return ワンタイムキーのリスト
     */
    fun getAllDigitalKeys(): List<CryptoGwDigitalKey>

    /**
     * すべてのotkを削除
     */
    fun deleteAllDigitalKeys()

    /**
     * Grant のすべての otk を削除
     *
     * @param grantId ロックID
     */
    fun deleteDigitalKeysByGrantId(
        grantId: String,
    )

    /**
     * Lock のすべての otk を削除
     *
     * @param lockId ロックID
     */
    fun deleteDigitalKeysByLockId(
        lockId: String,
    )

    /**
     * トークンをpreferenceに保持する
     *
     * @param token トークン
     */
    fun setToken(token: String)

    /**
     * SdkToken を削除
     */
    fun deleteToken()

    /**
     * SdkId を削除
     */
    fun deleteSdkId()

    /**
     * 全てのデジタルキーを削除する
     */
    fun deleteAll()

    fun clearSharedKeyConfig()
}
