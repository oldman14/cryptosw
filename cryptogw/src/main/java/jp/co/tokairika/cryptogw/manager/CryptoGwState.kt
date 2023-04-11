package jp.co.tokairika.cryptogw.manager

import android.app.Application
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.utils.NetworkUtils
import jp.co.tokairika.cryptosdk.DkManager

internal class CryptoGwState(private val application: Application) {

    /**
     * 初期化したか。
     * CryptoGw#initializeを実行済みであればtrue
     */
    val isInitialized: Boolean
        get() {
            return try {
                val isValidDeviceKey = DkManager.isValidDeviceKey()
                GwPreferenceUtils.sdkId.isNotEmpty() && isValidDeviceKey
            } catch (e: Exception) {
                false
            }

        }

    /**
     * ネットワークに接続しているか。
     * ネットワークに接続いればtrue
     */
    val isOnline: Boolean
        get() {
            return NetworkUtils.isConnected(application.applicationContext)
        }
}