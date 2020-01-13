package com.tim.iot.device.remote.protocol

import com.google.gson.annotations.SerializedName
import com.tim.iot.device.entity.QrCodeInfo

/**
 * QrCode
 *
 * @author Tell.Tim
 * @date 2019/12/27 19:11
 */
class QrCode {

    class Param {
        @SerializedName("deviceId")
        var deviceId: String? = null
        @SerializedName("timestamp")
        var timestamp: Long? = null
        var type: String? = null
    }

    class Result : BaseResult() {
        @SerializedName("qrCodeInfo")
        var qrCodeInfo: QrCodeInfo? = null
    }
}
