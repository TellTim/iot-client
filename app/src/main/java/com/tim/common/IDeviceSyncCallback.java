package com.tim.common;

import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.QrCodeInfo;

/**
 * IDeviceSyncCallback
 *
 * @author Tell.Tim
 * @date 2019/12/28 16:48
 * 同步远端授权的监听
 */
public interface IDeviceSyncCallback {
    void onSyncAuthorized(AccountInfo accountInfo);
    void onSyncUnAuthorized(QrCodeInfo qrCodeInfo);
    void onSyncError(Exception e);
}
