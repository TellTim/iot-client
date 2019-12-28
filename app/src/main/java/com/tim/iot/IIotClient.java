package com.tim.iot;

import com.tim.android.service.CoreService;
import com.tim.common.ICallback;
import com.tim.common.IDeviceSyncCallback;
import com.tim.common.Respond;
import com.tim.iot.common.AccountInfo;

/**
 * IIotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
public interface IIotClient {
    void checkLocalAuthorized(ICallback<String, Respond>callback);
    void getAccount();
    void saveAccount(AccountInfo accountInfo);
    void syncRemoteAuthorized(IDeviceSyncCallback syncCallback);
}
