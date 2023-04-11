package jp.co.tokairika.cryptogw

import io.mockk.*
import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.database.repository.DigitalKeyRepository
import jp.co.tokairika.cryptogw.entity.api.response.DigitalKeyIssuableGrants
import jp.co.tokairika.cryptogw.entity.api.response.ValidDigitalKeysResponse
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.sdkaccessor.DigitalKeyDeleter
import jp.co.tokairika.cryptogw.usecase.manager.PullDigitalKeyUseCase
import jp.co.tokairika.cryptosdk.DkManager
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class PullCryptoGwDigitalKeyUseCaseTest {
    lateinit var cryptoGwState: CryptoGwState
    lateinit var logOperationRepository: LogOperationRepository
    lateinit var digitalKeyRepository: DigitalKeyRepository
    lateinit var cryptoGWApiService: CryptoGWApiService
    lateinit var dkManager: DkManager
    lateinit var digitalKeyDeleter: DigitalKeyDeleter
    lateinit var sharePreference: GwPreferenceUtils
    lateinit var pullDigitalKeyUseCase: PullDigitalKeyUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        cryptoGwState = mockk()
        logOperationRepository = mockk()
        digitalKeyRepository = mockk()
        cryptoGWApiService = mockk()
        dkManager = mockk()
        digitalKeyDeleter = mockk()
        sharePreference = mockk()
    }

    @Test
    fun pullDigitalKey_digitalKeyIssuableGrants_failure() = runBlocking {
        pullDigitalKeyUseCase = PullDigitalKeyUseCase(
            cryptoGwState,
            logOperationRepository,
            digitalKeyRepository,
            cryptoGWApiService,
            dkManager,
            sharePreference,
            digitalKeyDeleter,
            "lockId",
            "grantId",
            "sequenceName"
        )
        every {
            sharePreference.sdkId
        } returns ""
        every {
            cryptoGwState.isInitialized
        } returns true
        every {
            logOperationRepository.create(
                sequenceName = any(),
                functionName = any(),
                lockId = any(),
                exception = any(),
                oneTimeKeyId = any()
            )
        } just Runs
        coEvery {
            logOperationRepository.sendToServer()
        } just Runs
        every { digitalKeyRepository.readAll() } returns emptyList()
        coEvery {
            cryptoGWApiService.validateKeysByGrantOrLock(any(), any(), any())
        } coAnswers {
            val errorCode = 500
            val errorResponse = Response.error<Any>(errorCode, ResponseBody.create("application/json".toMediaTypeOrNull(), ""))
            throw  HttpException(errorResponse)
        }
        val result = pullDigitalKeyUseCase.pullDigitalKey()
        println("result = ${result.resultType}")
        assert(result.resultType == ResultType.SERVER_ERROR)
    }

    @Test
    fun pullDigitalKey_digitalKeyIssuableGrants_success_downloadKey_failure() = runBlocking {
        pullDigitalKeyUseCase = PullDigitalKeyUseCase(
            cryptoGwState,
            logOperationRepository,
            digitalKeyRepository,
            cryptoGWApiService,
            dkManager,
            sharePreference,
            digitalKeyDeleter,
            "lockId",
            "grantId",
            "sequenceName"
        )
        every {
            sharePreference.sdkId
        } returns ""
        every {
            cryptoGwState.isInitialized
        } returns true
        every {
            logOperationRepository.create(
                sequenceName = any(),
                functionName = any(),
                lockId = any(),
                exception = any(),
                oneTimeKeyId = any()
            )
        } just Runs
        coEvery {
            logOperationRepository.sendToServer()
        } just Runs
        every { digitalKeyRepository.readAll() } returns emptyList()
        val listGrantForDownload = listOf(
            DigitalKeyIssuableGrants(grantId = "grantId1", null, null, null, null),
            DigitalKeyIssuableGrants(grantId = "grantId2", null, null, null, null),
            DigitalKeyIssuableGrants(grantId = "grantId3", null, null, null, null),
        )
        val responseIssuableGrants = ValidDigitalKeysResponse(
            validatedDigitalKeyResults = listOf(),
            digitalKeyIssuableGrants = listGrantForDownload,
            error = null
        )
        coEvery {
            cryptoGWApiService.validateKeysByGrantOrLock(any(), any(), any())
        } returns responseIssuableGrants
        every {
            digitalKeyRepository.readByGrantId(any())
        } returns emptyList()
        every {
            dkManager.generateOneTimeRequestToken()
        } returns "requestToken"
        coEvery { cryptoGWApiService.issueOneTimeKey(any(), any()) } coAnswers {
            val errorCode = 500
            val errorResponse = Response.error<Any>(errorCode, ResponseBody.create("application/json".toMediaTypeOrNull(), ""))
            throw  HttpException(errorResponse)
        }
        every {  digitalKeyDeleter.deleteOneTimeKeys(any()) } just Runs
        val result = pullDigitalKeyUseCase.pullDigitalKey()
        assert(result.resultType == ResultType.SERVER_ERROR)
    }
}