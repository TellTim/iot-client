package com.tim.iot.auth;

import com.tim.iot.device.entity.AccountInfo;

/**
 * IAuthServer
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
public interface IAuthServer {
    void connect(String param,int timeoutOfSecond,IConnectAuthServerCallback connectAuthServerCallback);

    void closeConnect(String url);

    interface IConnectAuthServerCallback{
        void onConnectSuccess();
        void onConnectError(Exception e);
        void onConfirm(AccountInfo accountInfo);
        void onTimeOut();
    }

}
