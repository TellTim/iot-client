package com.tim.common;

import com.tim.iot.common.AccountInfo;

/**
 * IConnectAuthServerCallback
 *
 * @author Tell.Tim
 * @date 2019/12/30 20:24
 */
public interface IConnectAuthServerCallback {
    void onConnectSuccess();
    void onAuthConfirm(AccountInfo accountInfo);
    void onConnectError(Exception e);
}
