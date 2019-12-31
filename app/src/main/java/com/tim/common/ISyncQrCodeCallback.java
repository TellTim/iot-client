package com.tim.common;

import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.QrCodeInfo;

/**
 * ISyncQrCodeCallback
 *
 * @author Tell.Tim
 * @date 2019/12/28 16:48
 * 同步远端授权的回调
 */
public interface ISyncQrCodeCallback {
    /**
     * 已经授权成功
     * @param accountInfo AccountInfo
     */
    void onSyncQrCodeAuthorized(AccountInfo accountInfo);
    /**
     * 同步到新的二维码
     * @param qrCodeInfo QrCodeInfo
     */
    void onSyncQrCodeInfo(QrCodeInfo qrCodeInfo);

    /**
     * 同步二维码异常
     * @param e Exception
     */
    void onSyncQrCodeError(Exception e);

    /**
     * 二维码过期
     */
    void onAuthTimeOut();
}
