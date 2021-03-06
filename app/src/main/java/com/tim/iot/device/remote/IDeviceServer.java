package com.tim.iot.device.remote;

import com.tim.common.ICallback;
import com.tim.common.Respond;
import com.tim.iot.device.entity.AccountInfo;
import com.tim.iot.common.DeviceInfo;

/**
 * IDeviceServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 11:09
 */
public interface IDeviceServer {
    void syncAuthorized(DeviceInfo deviceInfo, ICallback<AccountInfo, Respond> callback);

    void syncQrCode(DeviceInfo deviceInfo, ICallback<AccountInfo, Respond> callback);
}
