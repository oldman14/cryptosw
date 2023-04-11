package jp.co.tokairika.cryptogw.api

/**
 * トークンリフレッシュハンドラーのインターフェース
 */
interface TokenRefreshHandler {
    /**
     * トークンリフレッシュ処理
     *
     * @return トークンリフレッシュデータソース
     */
    suspend fun refreshTokenAsync(): String
}