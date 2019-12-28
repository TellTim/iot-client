package com.tim.iot.register;

import com.tim.common.ICallback;
import com.tim.common.Respond;
import com.tim.iot.common.DeviceInfo;

/**
 * IRegisterServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 11:09
 */
public interface IRegisterServer {
    void syncFromServer(DeviceInfo deviceInfo, ICallback<String, Respond>callback);
}