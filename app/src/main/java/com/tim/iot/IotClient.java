package com.tim.iot;

import android.content.Context;
import com.tim.common.DeviceUtils;
import com.tim.common.ICallback;
import com.tim.common.Logger;
import com.tim.common.Respond;
import com.tim.iot.auth.AuthServer;
import com.tim.iot.auth.IAuthServer;
import com.tim.iot.auth.entity.UrlInfo;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.device.entity.AccountInfo;
import com.tim.iot.device.entity.QrCodeInfo;
import com.tim.iot.device.local.ILocalServer;
import com.tim.iot.device.local.LocalServer;
import com.tim.iot.device.remote.DeviceService;
import com.tim.iot.device.remote.IDeviceServer;
import java.util.concurrent.ExecutorService;

/**
 * IotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
public class IotClient implements IIotClient {
    private static final Logger logger = Logger.getLogger("IotClient");
    private static IIotClient instance;
    private ExecutorService executorService;
    private Context context;
    private IAuthServer authServer;
    private DeviceInfo deviceInfo;
    private IDeviceServer registerServer;
    private ILocalServer localServer;
    private IotClient(Context context, ExecutorService executorService,
            DeviceInfo deviceInfo) {
        this.context = context;
        this.executorService = executorService;
        this.deviceInfo = deviceInfo;
        this.authServer = new AuthServer();
        this.registerServer = new DeviceService(executorService);
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
    public String getAccount() {
        return localServer.getAccount();
    }

    @Override
    public void syncAuthorized(ISyncAuthorizedCallback callback) {
        this.registerServer.syncAuthorized(this.deviceInfo, new ICallback<AccountInfo, Respond>() {
            @Override
            public void onSuccess(AccountInfo accountInfo) {
                logger.d("syncAuthorized onSuccess accountInfo " + accountInfo.toString());
                localServer.saveAuthToLocal(accountInfo.toString());
                callback.onSyncAuthorized(accountInfo);
            }

            @Override
            public void onFail(Respond respond) {
                if (Respond.State.BIND_NOT_EXIST.equals(respond.getState())) {
                    logger.d("syncAuthorized onFail bind not exist");
                    localServer.clearAuthorized();
                    callback.onSyncUnAuthorized();
                } else {
                    callback.onSyncAuthorizedError(new Exception(respond.getState().getValue()));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.e("syncAuthorized onError " + throwable.getMessage());
                if(BuildConfig.DEBUG_REGISTER) {
                    localServer.clearAuthorized();
                    callback.onSyncUnAuthorized();
                }else{
                    callback.onSyncAuthorizedError(new Exception(throwable.getMessage()));
                }
            }
        });
    }

    @Override
    public synchronized void syncQrCode(ISyncQrCodeCallback callback) {
        this.registerServer.syncQrCode(this.deviceInfo, new ICallback<AccountInfo, Respond>() {
            @Override
            public void onSuccess(AccountInfo accountInfo) {
                logger.d("syncQrCode Authorized accountInfo " + accountInfo.toString());
                localServer.saveAuthToLocal(accountInfo.toString());
                callback.onSyncQrCodeAuthorized(accountInfo);
            }

            @Override
            public void onFail(Respond respond) {
                //只将未授权的回调出去，其他异常需修复处理
                if (respond.getState().equals(Respond.State.BIND_NOT_EXIST)) {
                    logger.d("syncQrCode onSyncQrCodeInfo "
                            + ((QrCodeInfo) respond.getT()).getQrCode());
                    QrCodeInfo qrCodeInfo = (QrCodeInfo) respond.getT();
                    final UrlInfo urlInfo = new UrlInfo(DeviceUtils.getDeviceSerial());
                    executorService.execute(() -> authServer.connect(urlInfo.toString(),
                            qrCodeInfo.getExpireIn(), new IAuthServer.IConnectAuthServerCallback() {
                                @Override
                                public void onConnectSuccess() {
                                    logger.d("onConnectSuccess");
                                    callback.onSyncQrCodeInfo(qrCodeInfo);
                                }

                                @Override
                                public void onConfirm(AccountInfo accountInfo) {
                                    logger.d("onConfirm");
                                    localServer.saveAuthToLocal(accountInfo.toString());
                                    callback.onSyncQrCodeAuthorized(accountInfo);
                                    authServer.closeConnect(urlInfo.toString());
                                }

                                @Override
                                public void onTimeOut() {
                                    logger.d("onTimeOut");
                                    callback.onAuthTimeOut();
                                    authServer.forceClose();
                                }

                                @Override
                                public void onConnectError(Exception e) {
                                    logger.e("onConnectError "+e.getMessage());
                                    callback.onSyncQrCodeError(e);
                                }
                            }));
                } else {
                    //todo 此处应该埋点,通过trace-lib动态上传给后端,等候分析异常.
                    callback.onSyncQrCodeError(new Exception(((String) respond.getT())));
                    logger.e("syncQrCode onFail " + respond.getT());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                //todo 此处应该埋点,通过trace-lib动态上传给后端,等候分析异常.
                logger.e("syncQrCode onError " + throwable.getCause());
                if (BuildConfig.DEBUG_REGISTER){
                    QrCodeInfo qrCodeInfo = new QrCodeInfo();
                    qrCodeInfo.setExpireIn(120);
                    qrCodeInfo.setQrCode("adsfasdfasdfasdadfddd");
                    final UrlInfo urlInfo = new UrlInfo(DeviceUtils.getDeviceSerial());
                    executorService.execute(() -> authServer.connect(urlInfo.toString(),
                            qrCodeInfo.getExpireIn(), new IAuthServer.IConnectAuthServerCallback() {
                                @Override
                                public void onConnectSuccess() {
                                    logger.d("onConnectSuccess");
                                    callback.onSyncQrCodeInfo(qrCodeInfo);
                                }

                                @Override
                                public void onConfirm(AccountInfo accountInfo) {
                                    logger.d("onConfirm");
                                    localServer.saveAuthToLocal(accountInfo.toString());
                                    callback.onSyncQrCodeAuthorized(accountInfo);
                                    authServer.closeConnect(urlInfo.toString());
                                }

                                @Override
                                public void onTimeOut() {
                                    logger.d("onTimeOut");
                                    callback.onAuthTimeOut();
                                    authServer.forceClose();
                                }

                                @Override
                                public void onConnectError(Exception e) {
                                    logger.e("onConnectError "+e.getMessage());
                                    callback.onSyncQrCodeError(e);
                                }
                            }));
                }else{
                    callback.onSyncQrCodeError(new Exception(throwable.getMessage()));
                }

            }
        });
    }
}
