package com.tim.iot.common

import com.google.gson.annotations.SerializedName

/**
 * DeviceInfo
 *
 * @author Tell.Tim
 * @date 2019/12/25 17:42
 */
class DeviceInfo {
    @SerializedName("deviceId")
    var deviceId: String? = null
    var mac: String? = null
    var imei: String? = null
    var type: String? = null

    constructor() {}

    constructor(deviceId: String, mac: String, imei: String, type: String) {
        this.deviceId = deviceId
        this.mac = mac
        this.imei = imei
        this.type = type
    }

    override fun toString(): String {
        return String.format("deviceId=%s,mac=%s,imei=%s,type=%s", deviceId, mac, imei, type)
    }
}
