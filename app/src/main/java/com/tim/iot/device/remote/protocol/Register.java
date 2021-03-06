package com.tim.iot.device.remote.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * Register
 *
 * @author Tell.Tim
 * @date 2019/12/28 19:57
 */
public class Register {
    public static class Param {
        @SerializedName("deviceId")
        private String deviceId;
        private String imei;
        private String mac;
        @SerializedName("timestamp")
        private Long timestamp;
        private String type;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Result extends BaseResult{

    }
}
