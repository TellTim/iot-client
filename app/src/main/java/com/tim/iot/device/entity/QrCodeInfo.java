package com.tim.iot.device.entity;

import com.google.gson.annotations.SerializedName;

/**
 * QrCodeInfo
 *
 * @author Tell.Tim
 * @date 2019/12/28 14:31
 */
public class QrCodeInfo {
    @SerializedName("qrCode")
    private String qrCode;
    @SerializedName("expireIn")
    private int expireIn;

    public QrCodeInfo() {
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }
}
