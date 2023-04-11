package jp.co.tokairika.cryptogw

import jp.co.tokairika.cryptogw.utils.DateUtils
import org.junit.Test

import org.junit.Assert.*

/**
 * DateUtils unit test.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DateUtilsUnitTest {
    @Test
    fun getNow() {
        assertEquals(DateUtils.now(), "2023-01-03T12:51:17+09:00")
    }

    @Test
    fun isNowBetweenTrue() {
        assertEquals(DateUtils.isNowBetween("2023-01-03T12:41:17+09:00","2023-02-03T12:51:17+09:00"), true)
    }

    @Test
    fun isNowBetweenFalse() {
        assertEquals(DateUtils.isNowBetween("2023-01-01T12:51:17+09:00","2023-01-02T23:51:17+09:00"), false)
    }

    @Test
    fun isNowOver() {
        assertEquals(DateUtils.isNowOver("2023-01-01T12:51:17+09:00"), true)
    }
}