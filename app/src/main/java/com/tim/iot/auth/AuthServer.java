package com.tim.iot.auth;

import android.text.TextUtils;
import com.tim.common.IConnectAuthServerCallback;
import com.tim.common.Logger;
import com.tim.iot.BuildConfig;
import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.ws.Builder;
import com.tim.iot.ws.IClient;
import com.tim.iot.ws.WebSocketListener;
import java.nio.ByteBuffer;

/**
 * AuthServer
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:33
 */
public class AuthServer implements IAuthServer {

    private static final Logger logger = Logger.getLogger("AuthServer");
    private IClient client;
    private DeviceInfo deviceInfo;

    public AuthServer(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        this.client = new Builder().setShowLog(true, "AuthServer-client").build();
    }

    @Override
    public synchronized void connect(String param, int timeoutOfSecond,
            IConnectAuthServerCallback connectAuthServerCallback) {
        this.client.connect(String.format("%s?deviceId=%s", BuildConfig.AUTH_HOST, param),
                timeoutOfSecond,
                new WebSocketListener() {
                    @Override
                    public void onConnected() {
                        connectAuthServerCallback.onConnectSuccess();
                    }

                    @Override public void onReConnect() {
                    }

                    @Override
                    public void onConnectFailed(Throwable e) {
                        connectAuthServerCallback.onConnectError(new Exception(e));
                    }

                    @Override public void onDisconnect() {
                    }

                    @Override
                    public void onMessage(String message) {
                        if (!TextUtils.isEmpty(message) && message.startsWith("confirm")) {
                            String[] convertArray = message.split("#");
                            AccountInfo accountInfo = new AccountInfo();
                            accountInfo.setAccount(convertArray[1]);
                            accountInfo.setCreateAt(Long.valueOf(convertArray[2]));
                            connectAuthServerCallback.onAuthConfirm(accountInfo);
                        }
                    }

                    @Override public void onMessage(ByteBuffer bytes) {

                    }
                });
    }

    @Override
    public void closeConnect() {
        this.client.closeAll();
    }
}
