package jp.co.tokairika.cryptogw.jobworker

import kotlinx.coroutines.CoroutineDispatcher

internal interface JobWorker {
    suspend fun handle()
}