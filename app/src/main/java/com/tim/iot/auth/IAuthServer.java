package com.tim.iot.auth;

import com.tim.common.IConnectAuthServerCallback;

/**
 * IAuthServer
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
public interface IAuthServer {
    void connect(IConnectAuthServerCallback connectAuthServerCallback);
}
