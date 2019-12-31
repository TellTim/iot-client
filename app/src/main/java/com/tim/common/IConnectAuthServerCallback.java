package com.tim.common;

/**
 * IConnectAuthServerCallback
 *
 * @author Tell.Tim
 * @date 2019/12/30 20:24
 */
public interface IConnectAuthServerCallback {
    void onConnectSuccess();
    void onAuthConfirm();
    void onTimeOut();
    void onConnectError(Exception e);
}
