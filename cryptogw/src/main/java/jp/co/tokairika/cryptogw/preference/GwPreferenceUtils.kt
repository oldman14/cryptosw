package jp.co.tokairika.cryptogw.preference

import android.content.Context
import android.content.SharedPreferences
import jp.co.tokairika.cryptogw.manager.exception.CryptoGwBuilderException

/**
 * PreferenceManager共通処理
 */
internal object GwPreferenceUtils {
    private const val PREFERENCE_NAME = "gwSharePreference"
    private const val SDK_TOKEN = "sdkToken"
    private const val SDK_ID = "sdkId"
    private const val TIME_STORE_SHARE_KEY = "timeStoreShareKey"
    private const val DB_KEY = "db_key"
    private const val LAST_DATE_CALL_MOBILE_SETTING = "lastDateCallMobileSetting"
    private const val SHARED_KEY_UPDATE_INTERVAL = "sharedKeyUpdateInterval"

    private var sharedPreferences_: SharedPreferences? = null

    private val sharedPreferences: SharedPreferences
        get() {
            sharedPreferences_?.let {
                return it
            }
            throw CryptoGwBuilderException("sharedPreferences")
        }

    /**
     * Preference初期処理
     *
     * @param context コンテキスト
     */
    fun init(context: Context) {
        if (sharedPreferences_ != null) return
        try {
            sharedPreferences_ = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            throw CryptoGwBuilderException("sharedPreferences")
        }
    }

    /**
     * レルム データベースの暗号化キー
     */
    var databaseKey: String
        set(value) {
            sharedPreferences.edit().putString(DB_KEY, value).apply()
        }
        get() = sharedPreferences.getString(DB_KEY, "") ?: ""

    /**
     * SDK トークン
     */
    var sdkToken: String
        set(value) {
            sharedPreferences.edit().putString(SDK_TOKEN, value).apply()
        }
        get() {
            return sharedPreferences.getString(SDK_TOKEN, "") ?: ""
        }

    /**
     * SDK ID
     */
    var sdkId: String
        set(value) {
            sharedPreferences.edit().putString(SDK_ID, value).apply()
        }
        get() {
            return sharedPreferences.getString(SDK_ID, "") ?: ""
        }

    var timeStoreShareKey: Long
        set(value) {
            sharedPreferences.edit().putLong(TIME_STORE_SHARE_KEY, value).apply()
        }
        get() {
            return sharedPreferences.getLong(TIME_STORE_SHARE_KEY, 0) ?: 0
        }

    var lastDateCallMobileSetting: String
        set(value) {
            sharedPreferences.edit().putString(LAST_DATE_CALL_MOBILE_SETTING, value).apply()
        }
        get() {
            return sharedPreferences.getString(LAST_DATE_CALL_MOBILE_SETTING, "") ?: ""
        }

    var sharedKeyUpdateInterval: Int
        set(value) {
            sharedPreferences.edit().putInt(SHARED_KEY_UPDATE_INTERVAL, value).apply()
        }
        get() {
            return sharedPreferences.getInt(SHARED_KEY_UPDATE_INTERVAL, 0)
        }

    /**
     * プリファレンス破棄
     */
    fun clearAll() {
        sdkId = ""
        sdkToken = ""
        timeStoreShareKey = 0
        lastDateCallMobileSetting = ""
        sharedKeyUpdateInterval = 0
    }
}