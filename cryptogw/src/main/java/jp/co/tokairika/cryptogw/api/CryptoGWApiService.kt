package jp.co.tokairika.cryptogw.api

import jp.co.tokairika.cryptogw.entity.api.request.*
import jp.co.tokairika.cryptogw.entity.api.response.*
import jp.co.tokairika.cryptogw.entity.domain.LogOperationInfo
import jp.co.tokairika.cryptogw.preference.GwPreferenceUtils

internal class CryptoGWApiService : BaseApiService() {

    private val sdkId: String
        get() = GwPreferenceUtils.sdkId

    /**
     * get sdkId from sever
     */
    suspend fun issueSdkId(): IssueSdkIdResponse? =
        getNetworkResultWithHeaderToken {
            service.issueSdkId(it)
        }

    /**
     * create shareKey ECDH with sever
     * @param requestData is public key of client
     * response is a public of sever
     */
    suspend fun shareKey(requestData: String): ShareKeyResponse? =
        getNetworkResultWithHeaderToken {
            val request = ShareKeyRequest(sdkId, requestData)
            service.shareKey(request, it)
        }

    /**
     * get OTK by gantId
     * @param oneTimeKeyRequestToken generate from sdk
     */
    suspend fun issueOneTimeKey(
        grantId: String,
        oneTimeKeyRequestToken: String,
    ): IssueOneTimeKeyResponse? {
        val request = IssueOneTimeKeyRequest(
            sdkId,
            grantId,
            oneTimeKeyRequestToken,
        )
        return getNetworkResultWithHeaderToken {
            service.issueOneTimeKey(
                request,
                it
            )
        }
    }

    /**
     * get sever time for sync time box
     * @param requestTimeData get from box
     */
    suspend fun getServerTime(
        otkId: String,
        requestTimeData: String
    ): GetServerTimeResponse? = getNetworkResultWithHeaderToken {
        val request = GetServerTimeRequest(sdkId, otkId, requestTimeData)
        service.getServerTime(request, it)
    }

    /**
     * send data to sever and receive data encrypt
     * @param plainData generate from app
     */
    suspend fun getServerEncryptData(
        plainData: String,
        dkbSerialNo: String,
    ): EncryptDataResponse? = getNetworkResultWithHeaderToken {
        val request = EncryptDataRequest(dkbSerialNo, plainData)
        service.encryptData(request, it)
    }

    /**
     * send data encrypt to sever and receive data decrypt
     * @param data receive from box
     */
    suspend fun getSeverDecryptData(
        data: String,
        dkbSerialNo: String
    ): DecryptDataResponse? = getNetworkResultWithHeaderToken {
        val request = DecryptDataRequest(dkbSerialNo, data)
        service.decryptData(request, it)
    }

    /**
     * check valid and not valid by lockId
     * @param digitalKeyIds is a all keys of lockId
     */
    suspend fun validateKeysByGrantOrLock(
        lockId: String?,
        grantId: String?,
        digitalKeyIds: List<String>
    ): ValidDigitalKeysResponse? =
        getNetworkResultWithHeaderToken {
            val request = ValidateDigitalKeyByGrantOrLockRequest(
                sdkId = sdkId,
                lockId = lockId,
                grantId = grantId,
                digitalKeyIds = digitalKeyIds
            )
            service.validateDigitalKeyByLock(request, it)
        }

    /**
     * check valid and not valid without lockId or grantId
     * @param digitalKeyIds is a all valid keys
     */
    suspend fun validateKeys(
        digitalKeyIds: List<String>
    ): ValidKeysResponse? = getNetworkResultWithHeaderToken {
        val request = ValidateDigitalKeyRequest(
            sdkId = sdkId,
            digitalKeyIds = digitalKeyIds
        )
        service.validateDigitalKey(request, it)
    }

    /**
     * send logs operation to sever
     */
    suspend fun logService(logServiceRequest: List<LogOperationInfo>) =
        getNetworkResultWithHeaderToken {
            service.logService(logServiceRequest, it)
        }

    /**
     * Get time need store share key again
     */
    suspend fun mobileSettings(): MobileSettingsResponse? =
        getNetworkResultWithHeaderToken {
            service.mobileSettings(it)
        }


}