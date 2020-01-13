package com.tim.iot.device.remote.protocol

import com.google.gson.annotations.SerializedName
import com.tim.iot.device.entity.AccountInfo

/**
 * BaseResult
 *
 * @author Tell.Tim
 * @date 2019/12/27 20:30
 */
open class BaseResult {
    var code: String? = null
    var data: String? = null
    @SerializedName("accountInfo")
    var accountInfo: AccountInfo? = null
}
