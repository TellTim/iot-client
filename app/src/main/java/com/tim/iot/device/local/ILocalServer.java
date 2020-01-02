package com.tim.iot.device.local;

/**
 * ILocalServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 11:09
 */
public interface ILocalServer {

    String getAccount();

    void saveAuthToLocal(String accountInfo);

    void clearAuthorized();
}
