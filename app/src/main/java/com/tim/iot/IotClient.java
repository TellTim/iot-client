package com.tim.iot;

import android.content.Context;
import com.tim.common.ICallback;
import com.tim.common.Logger;
import com.tim.common.Respond;
import com.tim.iot.auth.AuthServer;
import com.tim.iot.auth.IAuthServer;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.local.ILocalServer;
import com.tim.iot.local.LocalServer;
import com.tim.iot.local.entity.AccountInfo;
import com.tim.iot.register.IRegisterServer;
import com.tim.iot.register.RegisterService;
import java.util.concurrent.ExecutorService;

/**
 * IotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
public class IotClient implements IIotClient{
    private static final Logger logger = Logger.getLogger("AuthServer");
    private static IIotClient instance;
    private ExecutorService executorService;
    private Context context;
    private IAuthServer authServer;
    private DeviceInfo deviceInfo;
    private IRegisterServer registerServer;
    private ILocalServer localServer;

    private IotClient(Context context, ExecutorService executorService,
            DeviceInfo deviceInfo) {
        this.context = context;
        this.executorService = executorService;
        this.deviceInfo = deviceInfo;
        this.authServer = new AuthServer(deviceInfo);
        this.registerServer = new RegisterService(executorService);
        this.localServer = new LocalServer(context);
    }

    public static IIotClient getInstance(Context context, ExecutorService executorService,
            DeviceInfo deviceInfo) {
        synchronized (AuthServer.class) {
            if (instance == null) {
                instance = new IotClient(context, executorService, deviceInfo);
            }
            return instance;
        }
    }

    @Override
    public void checkLocalAuthorized(ICallback<String, Respond> callback) {
        logger.d("checkLocalAuthorized");
        localServer.checkAuthFromLocal(new ICallback<String, Respond>() {
            @Override
            public void onSuccess(String account) {
                callback.onSuccess(account);
            }

            @Override
            public void onFail(Respond respond) {
                callback.onFail(respond);
            }

            @Override public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    @Override
    public void getAccount() {

    }

    @Override
    public void saveAccount(AccountInfo accountInfo) {

    }

    @Override
    public void syncRemoteAuthorized(ICallback<String, Respond> callback) {
        registerServer.syncFromServer(this.deviceInfo, new ICallback<String, Respond>() {
            @Override
            public void onSuccess(String s) {
                logger.d("syncRemoteAuthorized account "+s);
                localServer.saveAuthToLocal(s);
                callback.onSuccess(s);
            }

            @Override
            public void onFail(Respond respond) {
                callback.onFail(respond);
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }
}
