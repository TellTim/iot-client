package com.tim.iot;

import com.tim.common.IConnectAuthServerCallback;
import com.tim.common.ISyncAuthorizedCallback;
import com.tim.common.ISyncQrCodeCallback;

/**
 * IIotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
public interface IIotClient {
    String getAccount();

    void syncAuthorized(ISyncAuthorizedCallback callback);

    void syncQrCode(ISyncQrCodeCallback syncQrCodeCallback);

}
