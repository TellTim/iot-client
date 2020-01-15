package com.tim.iot.trace.domain.entity;

/**
 * AppInfo
 *
 * @author Tell.Tim
 * @date 2020/1/15 14:08
 */
public class AppInfo {
    private String appVersion;
    private String deviceId;

    public String getAppVersion() {
        return appVersion;
    }

    public AppInfo(String appVersion, String deviceId) {
        this.appVersion = appVersion;
        this.deviceId = deviceId;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
