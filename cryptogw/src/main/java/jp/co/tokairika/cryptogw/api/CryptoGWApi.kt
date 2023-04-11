package jp.co.tokairika.cryptogw.api

import jp.co.tokairika.cryptogw.entity.api.request.*
import jp.co.tokairika.cryptogw.entity.api.response.*
import jp.co.tokairika.cryptogw.entity.domain.LogOperationInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface CryptoGWApi {
    @POST("/v1/issue_sdk_id")
    suspend fun issueSdkId(
        @Header("Authorization") authorization: String
    ): Response<IssueSdkIdResponse>

    @POST("/v1/revoke_sdk_id")
    suspend fun revokeSdkId(
        @Body revokeSdkIdRequest: RevokeSdkIdRequest,
        @Header("Authorization") authorization: String
    ): Response<Any>

    @POST("/v1/share_key")
    suspend fun shareKey(
        @Body shareKeyRequest: ShareKeyRequest,
        @Header("Authorization") authorization: String
    ): Response<ShareKeyResponse>

    @POST("/v1/download_digital_key")
    suspend fun issueOneTimeKey(
        @Body issueOneTimeKeyRequest: IssueOneTimeKeyRequest,
        @Header("Authorization") authorization: String
    ): Response<IssueOneTimeKeyResponse>

    @POST("/v1/get_server_time")
    suspend fun getServerTime(
        @Body getServerTimeRequest: GetServerTimeRequest,
        @Header("Authorization") authorization: String
    ): Response<GetServerTimeResponse>

    @POST("/v1/EncryptData")
    suspend fun encryptData(
        @Body encryptDataRequest: EncryptDataRequest,
        @Header("Authorization") authorization: String
    ): Response<EncryptDataResponse>

    @POST("/v1/DecryptData")
    suspend fun decryptData(
        @Body decryptDataRequest: DecryptDataRequest,
        @Header("Authorization") authorization: String
    ): Response<DecryptDataResponse>

    @POST("/v1/digital_key_issuable_grants")
    suspend fun validateDigitalKeyByLock(
        @Body validateDigitalKeyByGrantOrLockRequest: ValidateDigitalKeyByGrantOrLockRequest,
        @Header("Authorization") authorization: String
    ): Response<ValidDigitalKeysResponse>

    @POST("/v1/digital_key_valid")
    suspend fun validateDigitalKey(
        @Body validateDigitalKeyRequest: ValidateDigitalKeyRequest,
        @Header("Authorization") authorization: String
    ): Response<ValidKeysResponse>

    @POST("/v1/operation_logs")
    suspend fun logService(
        @Body logOperationInfo: List<LogOperationInfo>,
        @Header("Authorization") authorization: String
    ): Response<Void>

    @POST("v1/mobile_settings")
    suspend fun mobileSettings(
        @Header("Authorization") authorization: String
    ): Response<MobileSettingsResponse>


}