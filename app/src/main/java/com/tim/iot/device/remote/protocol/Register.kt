package com.tim.iot.device.remote.protocol

import com.google.gson.annotations.SerializedName

/**
 * Register
 *
 * @author Tell.Tim
 * @date 2019/12/28 19:57
 */
class Register {
    class Param {
        @SerializedName("deviceId")
        var deviceId: String? = null
        var imei: String? = null
        var mac: String? = null
        @SerializedName("timestamp")
        var timestamp: Long? = null
        var type: String? = null
    }

    class Result : BaseResult()
}
