package com.tim.common;

import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.QrCodeInfo;

/**
 * ISyncAuthorizedCallback
 *
 * @author Tell.Tim
 * @date 2019/12/28 18:25
 *  同步远端设备是否已经授权成功的回调
 */
public interface ISyncAuthorizedCallback {
    void onSyncAuthorized(AccountInfo accountInfo);
    void onSyncUnAuthorized();
    void onSyncError(Exception e);
}
