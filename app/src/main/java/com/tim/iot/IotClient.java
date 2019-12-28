package com.tim.iot;

import android.content.Context;
import com.tim.common.ICallback;
import com.tim.common.ISyncAuthorizedCallback;
import com.tim.common.ISyncQrCodeCallback;
import com.tim.common.Logger;
import com.tim.common.Respond;
import com.tim.iot.auth.AuthServer;
import com.tim.iot.auth.IAuthServer;
import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.common.QrCodeInfo;
import com.tim.iot.local.ILocalServer;
import com.tim.iot.local.LocalServer;
import com.tim.iot.register.IRegisterServer;
import com.tim.iot.register.RegisterService;
import java.util.concurrent.ExecutorService;

/**
 * IotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
public class IotClient implements IIotClient {
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
    public void syncAuthorized(ISyncAuthorizedCallback callback) {

    }

    @Override
    public void syncQrCode(ISyncQrCodeCallback callback) {
        registerServer.syncQrCode(this.deviceInfo, new ICallback<AccountInfo, Respond>() {
            @Override
            public void onSuccess(AccountInfo accountInfo) {
                logger.d("syncRemoteAuthorized onSuccess accountInfo " + accountInfo.toString());
                localServer.saveAuthToLocal(accountInfo.toString());
                callback.onSyncQrCodeAuthorized(accountInfo);
            }

            @Override
            public void onFail(Respond respond) {
                //只将未授权的回调出去，其他异常需修复处理
                if (respond.getState().equals(Respond.State.BIND_NOT_EXIST)) {
                    logger.d("syncRemoteAuthorized onSyncQrCode "
                            + ((QrCodeInfo) respond.getT()).getQrCode());
                    callback.onSyncQrCode((QrCodeInfo) respond.getT());
                } else {
                    //todo 此处应该埋点,通过trace-lib动态上传给后端,等候分析异常.
                    callback.onSyncQrCodeError(new Exception(((String) respond.getT())));
                    logger.e("syncRemoteAuthorized onFail " + ((String) respond.getT()));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                //todo 此处应该埋点,通过trace-lib动态上传给后端,等候分析异常.
                logger.e("syncRemoteAuthorized onError " + throwable.getCause());
                callback.onSyncQrCodeError(new Exception(throwable.getMessage()));
            }
        });
    }
}
