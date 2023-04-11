package jp.co.tokairika.cryptogw.jobworker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class JobWorkerQueue(
    private val coroutineScope: CoroutineScope
) {
    /** 同期インスタンス */
    private val mutex = Mutex()

    fun post(jobWorker: JobWorker) = coroutineScope.launch {
        mutex.withLock {
            jobWorker.handle()
        }
    }
}