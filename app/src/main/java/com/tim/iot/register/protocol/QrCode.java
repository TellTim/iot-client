package com.tim.iot.register.protocol;

import com.google.gson.annotations.SerializedName;
import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.QrCodeInfo;

/**
 * QrCode
 *
 * @author Tell.Tim
 * @date 2019/12/27 19:11
 */
public class QrCode {

    public static class Param {
        @SerializedName("deviceId")
        private String deviceId;
        @SerializedName("timestamp")
        private Long timestamp;
        private String type;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
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

    public static class Result extends BaseResult {
        @SerializedName("qrCodeInfo")
        private QrCodeInfo qrCodeInfo;

        public QrCodeInfo getQrCodeInfo() {
            return qrCodeInfo;
        }

        public void setQrCodeInfo(QrCodeInfo qrCodeInfo) {
            this.qrCodeInfo = qrCodeInfo;
        }
    }
}
