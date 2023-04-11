package jp.co.tokairika.cryptogw.sampleApp

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.tokairika.cryptogw.sampleApp.databinding.ActivityMainBinding
import jp.co.tokairika.cryptogw.security.GwKeyStore


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvHelloWorld.setOnClickListener {
            test()
        }
    }

    private fun test() {
        val sdkToken =
            "ZTVlIiwiaWF0IjoxNjc5Mjc4NDMwLCJleHAiOjE2NzkyODIwMzB9.sbAwJoNspr-BTBcLspWVds8e1u-tVn4rCT_BqcPKnzM"
        Log.d("cvt", "sdktoken = $sdkToken")
        val sdkByteDecode = Base64.decode(sdkToken, Base64.DEFAULT)
        val keyStore = GwKeyStore()
        val encrypt = keyStore.encrypt(sdkToken.toByteArray(), GwKeyStore.Alias.SDK_TOKEN)
        Log.d("cvt", "encrypt = $encrypt")
        val decrypt = keyStore.decrypt(encrypt, GwKeyStore.Alias.SDK_TOKEN)
        Log.d("cvt", "decrypt2 = ${decrypt.toString(Charsets.UTF_8)}")
    }


    private fun log(string: String) {
    }
}