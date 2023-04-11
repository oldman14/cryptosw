package jp.co.tokairika.cryptogw.entity.db


import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal open class CryptoGwDigitalKeyRealmModel : RealmObject {
    @PrimaryKey
    var otkId: String = ""
    var grantId: String = ""
    var lockId: String = ""
    var bleDeviceId: String = ""
    var serviceUuid: String = ""
    var useStartDate: String = ""
    var useEndDate: String = ""
    var slotId: String = ""
    var publishDate : String = ""

    constructor()
    constructor(
        otkId: String?,
        grantId: String?,
        lockId: String?,
        bleDeviceId: String?,
        serviceUuid: String?,
        useStartDate: String?,
        useEndDate: String?,
        slotId: String?,
        publishDate: String?
    ) {
        this.otkId = otkId ?: ""
        this.grantId = grantId ?: ""
        this.lockId = lockId ?: ""
        this.bleDeviceId = bleDeviceId ?: ""
        this.serviceUuid = serviceUuid ?: ""
        this.useStartDate = useStartDate ?: ""
        this.useEndDate = useEndDate ?: ""
        this.slotId = slotId ?: ""
        this.publishDate = publishDate ?: ""
    }

    companion object {
        const val GRANT_ID = "grantId"
        const val LOCK_ID = "lockId"
        const val OTK_ID = "otkId"
    }
}

