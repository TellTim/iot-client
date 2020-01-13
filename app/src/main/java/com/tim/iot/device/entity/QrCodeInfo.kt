package com.tim.iot.device.entity

import com.google.gson.annotations.SerializedName

/**
 * QrCodeInfo
 *
 * @author Tell.Tim
 * @date 2019/12/28 14:31
 */
class QrCodeInfo {
    @SerializedName("qrCode")
    var qrCode: String? = null
    @SerializedName("expireIn")
    var expireIn: Int = 0
}
