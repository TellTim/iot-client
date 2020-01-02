package com.tim.iot.auth.entity;

import com.tim.iot.BuildConfig;

/**
 * UrlInfo
 *
 * @author Tell.Tim
 * @date 2020/1/2 18:04
 */
public class UrlInfo {
    private String deviceId;

    public UrlInfo(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return String.format("%s?deviceId=%s", BuildConfig.AUTH_HOST, this.deviceId);
    }
}
