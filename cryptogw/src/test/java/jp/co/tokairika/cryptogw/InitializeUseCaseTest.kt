package jp.co.tokairika.cryptogw

import io.mockk.*
import jp.co.tokairika.cryptogw.api.CryptoGWApiService
import jp.co.tokairika.cryptogw.database.repository.LogOperationRepository
import jp.co.tokairika.cryptogw.entity.api.response.IssueSdkIdResponse
import jp.co.tokairika.cryptogw.entity.api.response.ShareKeyResponse
import jp.co.tokairika.cryptogw.manager.CryptoGwState
import jp.co.tokairika.cryptogw.manager.ResultType
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils
import jp.co.tokairika.cryptogw.usecase.manager.InitializeUseCase
import jp.co.tokairika.cryptosdk.DkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal class InitializeUseCaseTest {

    lateinit var initializeUseCase: InitializeUseCase
    lateinit var cryptoGwState: CryptoGwState
    lateinit var logOperationRepository: LogOperationRepository
    lateinit var cryptoGWApiService: CryptoGWApiService
    lateinit var dkManager: DkManager
    lateinit var sharePreference: GwPreferenceUtils

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        cryptoGwState = mockk()
        logOperationRepository = mockk()
        cryptoGWApiService = mockk()
        dkManager = mockk()
        sharePreference = mockk()
    }

    @Test
    fun initialize_issueSdkId_timeout() = runBlocking {
        coEvery { cryptoGWApiService.refreshNewToken() } coAnswers {}
        coEvery { cryptoGwState.isInitialized } returns false
        coEvery { cryptoGWApiService.issueSdkId() } coAnswers {
            delay(5000)
            throw SocketTimeoutException()
        }
        coEvery { logOperationRepository.sendToServer() } coAnswers {}
        every {
            logOperationRepository.create(
                sequenceName = "initialize",
                functionName = "InitializeUseCase.initialize",
                lockId = "",
                exception = any()
            )
        } coAnswers {}
        initializeUseCase = InitializeUseCase(cryptoGwState, logOperationRepository, cryptoGWApiService, dkManager, sharePreference)
        val result = initializeUseCase.initialize()
        assert(result.resultType == ResultType.TIMEOUT_ERROR)
    }

    @Test
    fun initialize_issueSdkId_unknownHost() = runBlocking {
        coEvery { cryptoGWApiService.refreshNewToken() } coAnswers {}
        coEvery { cryptoGwState.isInitialized } returns false
        coEvery { cryptoGWApiService.issueSdkId() } coAnswers {
            throw UnknownHostException()
        }
        coEvery { logOperationRepository.sendToServer() } coAnswers {}
        every {
            logOperationRepository.create(
                sequenceName = "initialize",
                functionName = "InitializeUseCase.initialize",
                lockId = "",
                exception = any()
            )
        } coAnswers {}
        initializeUseCase = InitializeUseCase(cryptoGwState, logOperationRepository, cryptoGWApiService, dkManager, sharePreference)
        val result = initializeUseCase.initialize()
        assert(result.resultType == ResultType.NETWORK_ERROR)
    }

    @Test
    fun initialize_issueSdkId_success_shareKey_Timeout() = runBlocking {
        coEvery { cryptoGWApiService.refreshNewToken() } coAnswers {}
        coEvery { cryptoGwState.isInitialized } returns false
        coEvery { logOperationRepository.sendToServer() } coAnswers {}
        every {
            logOperationRepository.create(
                sequenceName = any(),
                functionName = any(),
                lockId = any(),
                exception = any()
            )
        } coAnswers {}
        val issueSdkIdResponse = IssueSdkIdResponse(sdkId = "sdkId")
        coEvery { cryptoGWApiService.issueSdkId() } returns issueSdkIdResponse
        coEvery { cryptoGWApiService.shareKey(issueSdkIdResponse.sdkId) } coAnswers {
            delay(5000)
            throw SocketTimeoutException()
        }
        every { sharePreference.sdkId = any() } just Runs
        every { dkManager.generatePublishKey() } returns  "test"
        initializeUseCase = InitializeUseCase(cryptoGwState, logOperationRepository, cryptoGWApiService, dkManager, sharePreference)
        val result = initializeUseCase.initialize()
        assert(result.resultType == ResultType.TIMEOUT_ERROR)
    }

    @Test
    fun initialize_issueSdkId_success_shareKey_unknownHost() = runBlocking {
        coEvery { cryptoGWApiService.refreshNewToken() } coAnswers {}
        coEvery { cryptoGwState.isInitialized } returns false
        coEvery { logOperationRepository.sendToServer() } coAnswers {}
        every {
            logOperationRepository.create(
                sequenceName = any(),
                functionName = any(),
                lockId = any(),
                exception = any()
            )
        } coAnswers {}
        val issueSdkIdResponse = IssueSdkIdResponse(sdkId = "sdkId")
        coEvery { cryptoGWApiService.issueSdkId() } returns issueSdkIdResponse
        coEvery { cryptoGWApiService.shareKey(issueSdkIdResponse.sdkId) } coAnswers {
            throw UnknownHostException()
        }
        every { sharePreference.sdkId = any() } just Runs
        every { dkManager.generatePublishKey() } returns  "test"
        initializeUseCase = InitializeUseCase(cryptoGwState, logOperationRepository, cryptoGWApiService, dkManager, sharePreference)
        val result = initializeUseCase.initialize()
        assert(result.resultType == ResultType.NETWORK_ERROR)
    }

    @Test
    fun initialize_success() = runBlocking {
        coEvery { cryptoGWApiService.refreshNewToken() } coAnswers {}
        coEvery { cryptoGwState.isInitialized } returns false
        coEvery { logOperationRepository.sendToServer() } coAnswers {}
        every {
            logOperationRepository.create(
                sequenceName = any(),
                functionName = any(),
                lockId = any(),
                exception = any()
            )
        } coAnswers {}
        val issueSdkIdResponse = IssueSdkIdResponse(sdkId = "sdkId")
        coEvery { cryptoGWApiService.issueSdkId() } returns issueSdkIdResponse
        coEvery { cryptoGWApiService.shareKey(issueSdkIdResponse.sdkId) } returns ShareKeyResponse("shareKeyResponse")
        every { sharePreference.sdkId = any() } just Runs
        every { dkManager.generatePublishKey() } returns  "test"
        every { dkManager.storeSharedKey(any()) } just Runs
        initializeUseCase = InitializeUseCase(cryptoGwState, logOperationRepository, cryptoGWApiService, dkManager, sharePreference)
        val result = initializeUseCase.initialize()
        assert(result.resultType == ResultType.SUCCESS)
    }

}