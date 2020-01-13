package com.tim.iot.auth.entity

import com.tim.iot.BuildConfig

/**
 * UrlInfo
 *
 * @author Tell.Tim
 * @date 2020/1/2 18:04
 */
class UrlInfo(var deviceId: String?) {

    override fun toString(): String {
        return String.format("%s?deviceId=%s", BuildConfig.AUTH_HOST, this.deviceId)
    }
}
