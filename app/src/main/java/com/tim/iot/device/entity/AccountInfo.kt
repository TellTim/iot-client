package com.tim.iot.device.entity

import com.google.gson.annotations.SerializedName

/**
 * AccountInfo
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:54
 */
class AccountInfo {
    @SerializedName("account")
    var account: String? = null
    @SerializedName("createAt")
    var createAt: Long? = null

    override fun toString(): String {
        return String.format("%s&%s", this.account, this.createAt!!.toString())
    }
}
