package jp.co.tokairika.cryptogw.manager

import android.app.Application
import jp.co.tokairika.cryptogw.api.CryptoGWApiProvider
import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.api.TokenRefreshHandler
import jp.co.tokairika.cryptogw.database.RealmConfigure
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.sdkaccessor.DkConnectOption
import jp.co.tokairika.cryptosdk.DkManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * CryptoGw のビルダークラス
 *
 */
class CryptoGwManagerBuilder {

    // トークンのリフレッシュ処理を保持して、Managerで使用する
    private var tokenRefreshHandler: TokenRefreshHandler = object : TokenRefreshHandler {
        override suspend fun refreshTokenAsync(): String {
            return ""
        }

    }

    // DK接続オプション
    private var dkConnectOption = DkConnectOption()

    private var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main

    /**
     * トークンリフレッシュハンドラーをセットする
     *
     * @param tokenRefreshHandler トークンリフレッシュハンドラー
     * @return CryptoGWのビルダークラス
     */
    fun setTokenRefreshHandler(tokenRefreshHandler: TokenRefreshHandler): CryptoGwManagerBuilder {
        this.tokenRefreshHandler = tokenRefreshHandler
        return this
    }

    /**
     * 接続オプションを設定する。
     *
     * @param dkConnectOption 接続オプション
     * @return CryptoGWのビルダークラス
     */
    fun setDkConnectOption(dkConnectOption: DkConnectOption): CryptoGwManagerBuilder {
        this.dkConnectOption = dkConnectOption
        return this
    }

    fun setCallbackDispatcher(dispatcher: CoroutineDispatcher): CryptoGwManagerBuilder {
        this.coroutineDispatcher = dispatcher
        return this
    }

    /**
     * CryptoGW をビルドする
     * @property application アプリケーション
     * @return CryptoGWManager
     */
    fun build(application: Application): CryptoGwManager {
        GwPreferenceUtils.init(application.applicationContext)
        RealmConfigure.init()
        DkManager.initialize(application.applicationContext)
        CryptoGWApiProvider.init(tokenRefreshHandler)
        return CryptoGwManagerImp(
            dkConnectOption = dkConnectOption,
            dkManager = DkManager,
            logOperationRepository = LogOperationRepository(),
            digitalKeyRepository = DigitalKeyRepository(),
            cryptoGwState = CryptoGwState(application),
            cryptoGWApiService = CryptoGWApiService(),
            coroutineDispatcher
        )
    }
}