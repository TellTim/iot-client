package com.tim.iot.common;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

/**
 * DeviceInfo
 *
 * @author Tell.Tim
 * @date 2019/12/25 17:42
 */
public class DeviceInfo {
    @SerializedName("deviceId")
    private String deviceId;
    private String mac;
    private String imei;
    private String type;

    public DeviceInfo() {
    }

    public DeviceInfo(String deviceId, String mac, String imei, String type) {
        this.deviceId = deviceId;
        this.mac = mac;
        this.imei = imei;
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("deviceId=%s,mac=%s,imei=%s,type=%s",deviceId,mac,imei,type);
    }
}
