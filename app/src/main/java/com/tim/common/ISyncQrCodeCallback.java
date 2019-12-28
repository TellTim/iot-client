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
    void onSyncQrCodeAuthorized(AccountInfo accountInfo);
    void onSyncQrCode(QrCodeInfo qrCodeInfo);
    void onSyncQrCodeError(Exception e);
}
