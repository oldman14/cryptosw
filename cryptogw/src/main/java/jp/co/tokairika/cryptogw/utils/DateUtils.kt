package jp.co.tokairika.cryptogw.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日付関連処理用
 */
internal object DateUtils {

    /** 日時のフォーマット */
    private const val DATE_NORMAL_FORMAT = "yyyy-MM-dd"
    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+09:00"
    private const val DATE_MILLISECOND_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS+09:00"
    private const val JP_MILLISECOND = 9 * 3600 * 1000 // JP : GMT + 09:00
    private const val TIME_ZONE_JP = "GMT+9"

    /**
     * GMT + 09:00での現在時刻文字列を取得する
     *
     * @return 文字列の現在時刻
     */
    fun now(): String {
        // システム時刻からGMT+09:00の時刻オフセットを減算
        val formatter = SimpleDateFormat(DATE_FORMAT, Locale.JAPAN).apply {
            timeZone = TimeZone.getTimeZone(TIME_ZONE_JP)
        }
        return formatter.format(Date())
    }

    fun nowLogOperation(): String {
        // システム時刻からGMT+09:00の時刻オフセットを減算
        val formatter = SimpleDateFormat(DATE_MILLISECOND_FORMAT, Locale.JAPAN).apply {
            timeZone = TimeZone.getTimeZone(TIME_ZONE_JP)
        }
        return formatter.format(Date())
    }

    /**
     * 現在時刻(JP:GMT+09:00)がfrom-to(JP:GMT+09:00)の間にあるかどうか
     *
     * @param from 開始日時 "yyyy-MM-dd'T'HH:mm:ss'Z'"のフォーマット
     * @param to 終了日時 "yyyy-MM-dd'T'HH:mm:ss'Z'"のフォーマット
     * @return 現在時刻がfrom-to内かどうか
     */
    fun isNowBetween(from: String, to: String): Boolean {
        // フォーマットは一致しているので文字列のまま比較
        return (now() in from..to)
    }

    /**
     * 現在時刻(JP:GMT+09:00)がtargetDate(JP:GMT+09:00)を過ぎているかどうか
     *
     * @param targetDate 指定日時 "yyyy-MM-dd'T'HH:mm:ss'Z'"のフォーマット
     * @return 現在時刻が終了日時を過ぎているかどうか
     */
    fun isNowOver(targetDate: String): Boolean {
        return now() > targetDate
    }

    fun currentTime(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_JP)).timeInMillis
    }

    fun convertTimeToString(time : Long): String {
        // システム時刻からGMT+09:00の時刻オフセットを減算
        val formatter = SimpleDateFormat(DATE_NORMAL_FORMAT, Locale.JAPAN).apply {
            timeZone = TimeZone.getTimeZone(TIME_ZONE_JP)
        }
        return formatter.format(time)
    }
}
