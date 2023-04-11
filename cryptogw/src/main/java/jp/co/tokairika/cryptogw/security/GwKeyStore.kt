package jp.co.tokairika.cryptogw.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import jp.co.tokairika.cryptogw.manager.exception.CryptoGwKeyStoreException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * DkbKeyStoreクラス
 */
class GwKeyStore {


    companion object {
        private const val PROVIDER = "AndroidKeyStore"

        // JavaのRSAではECB以外は例外となるため、ECBを設定している
        private const val ALGORITHM = "AES/GCM/NoPadding"
    }

    /**
     * KeyStoreに登録する鍵のエイリアス
     *
     * @property value
     */
    enum class Alias(val value: String) {
        SDK_TOKEN("gw_sdk_token")
    }


    /**
     * ByteArray暗号化
     *
     * @param data 暗号化したいデータ
     * @param alias キーペアを識別するためのエイリアス。用途ごとに一意にする
     * @return 暗号化されBase64でラップ化された文字列
     */
    fun encrypt(data: ByteArray, alias: Alias): String {
        try {
            // Android KeyStore インスタンスの取得＆ロード

            KeyStore.getInstance(PROVIDER).apply {
                // Android KeyStore ロード
                load(null)
                // 指定エイリアスのKeyStore鍵が存在しない場合は生成する
                if (!containsAlias(alias.value)) {
                    createKeyIfNotExist(alias)
                }

                // keystoreから公開鍵取得
                val secretKey = getKey(alias.value, null)
//                 暗号化
                Cipher.getInstance(ALGORITHM).apply {
                    Log.d("cvt", "getInstance")

                    init(Cipher.ENCRYPT_MODE, secretKey)
                    val bytes = doFinal(data)
                    val ivCiphertext = ByteArray(iv.size + bytes.size)
                    System.arraycopy(iv, 0, ivCiphertext, 0, iv.size)
                    System.arraycopy(bytes, 0, ivCiphertext, iv.size, bytes.size)
                    // SharedPreferenceで保存しやすいようにBase64でString化
                    return Base64.encodeToString(ivCiphertext, Base64.DEFAULT)
                }
            }
        } catch (e: Exception) {
            // Keystore暗号化失敗
            throw CryptoGwKeyStoreException(e.toString())
        }
    }

    /**
     * 暗号化されたテキストを復号
     *
     * @param encText encryptで暗号化されたテキスト
     * @param alias キーペアを識別するためのエイリアス。用途ごとに一意にする
     * @return encTextを復号した平文
     */
    fun decrypt(encText: String, alias: Alias): ByteArray {

        try {
            // Android KeyStore インスタンスの取得
            KeyStore.getInstance(PROVIDER).apply {
                // Android KeyStore ロード
                load(null)
                // 該当のエイリアスが登録されていない場合、エラー
                if (!containsAlias(alias.value)) {
                    throw CryptoGwKeyStoreException("KEYSTORE_KEY_NOT_EXIST")
                }

                // 該当のエイリアスが登録されている場合のみ実行
                // BASE64でデコード
                val bytes = Base64.decode(encText, Base64.DEFAULT)

                // keystoreから秘密鍵取得
                val iv = bytes.copyOfRange(0, 12) // Separate IV
                val ciphertext = bytes.copyOfRange(
                    12,
                    bytes.size
                )
                val secretKey = getKey(alias.value, null)
                // 復号
                Cipher.getInstance(ALGORITHM).apply {
                    init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
                    return doFinal(ciphertext)
                }
            }
        } catch (e: Exception) {
            // Keystore復号失敗
            throw CryptoGwKeyStoreException(e.toString())
        }
    }

    /**
     * 登録されているKeyStore鍵を全て削除する
     */
    fun deleteAllKey() {

        try {
            // Android KeyStore インスタンスの取得
            KeyStore.getInstance(PROVIDER).apply {
                // Android KeyStore ロード
                load(null)
                // Enum定義のエイリアスを全削除
                Alias.values().forEach {
                    if (containsAlias(it.value)) {
                        // 鍵が登録されていたら削除する
                        deleteEntry(it.value)
                    }
                }
                // 鍵が登録されていたら削除する
            }
        } catch (e: Exception) {
            // Keystore全削除失敗
            throw CryptoGwKeyStoreException(e.toString())
        }
    }

    /**
     * 指定エイリアスの鍵が存在しないとき、Keystore鍵を生成する
     *
     */
    private fun createKeyIfNotExist(alias: Alias) {

        // 作成する鍵ペアのスペックを指定するため、KeyGenParameterSpec生成
        val parameterSpecBuilder =
            KeyGenParameterSpec.Builder(
                alias.value,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
        val parameterSpec = parameterSpecBuilder
            .run {
                // RSAモードの設定
//                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                // JavaのRSAではECB以外は例外となるため、ECBを設定している
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // 鍵利用に端末認証を必要としない設定
                setUserAuthenticationRequired(false)
                build()
            }
        Log.d("cvt", "publicKey = createKeyIfNotExist")

        // 鍵ペアを作成するKeyPairGeneratorSpi
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            .apply {
                // 指定したKeyGenParameterSpecでKeyPairGeneratorを初期化
                init(parameterSpec)
                // 鍵ペア生成
                generateKey()
            }
    }

}
