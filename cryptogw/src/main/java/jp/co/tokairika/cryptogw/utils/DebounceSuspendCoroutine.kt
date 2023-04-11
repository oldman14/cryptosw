package jp.co.tokairika.cryptogw.utils

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

private class DebounceContinuation<T>(val continuation: Continuation<T>): Continuation<T> {
    override val context: CoroutineContext
        get() = continuation.context

    var isResumed = false

    override fun resumeWith(result: Result<T>) {
        if (isResumed) return
        isResumed = true
        continuation.resumeWith(result)
    }
}

/**
 * 一度しか実行できないContinuationを生成する。
 */
internal suspend fun <T> debounceSuspendCoroutine(block: (Continuation<T>) -> Unit): T {
    return suspendCoroutine { continuation ->
        val debounceContinuation = DebounceContinuation<T>(continuation)
        block(debounceContinuation)
    }
}
