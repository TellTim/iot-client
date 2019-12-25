package com.tim.iot.auth;

import android.content.Context;
import com.tim.common.Logger;
import com.tim.iot.BuildConfig;
import com.tim.iot.auth.view.IAuthView;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.ws.Builder;
import com.tim.iot.ws.IClient;
import com.tim.iot.ws.IConnectCallback;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

/**
 * IotClient
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:33
 */
public class IotClient implements IIotClient {

    private static final Logger logger = Logger.getLogger("IotClient");
    private static IIotClient instance;
    private ExecutorService executorService;
    private Context context;
    private IAuthView authView;
    private IClient client;
    private DeviceInfo deviceInfo;

    private IotClient(Context context, ExecutorService executorService, IAuthView authView,
            DeviceInfo deviceInfo) {
        this.context = context;
        this.executorService = executorService;
        this.authView = authView;
        this.deviceInfo = deviceInfo;
        this.client = new Builder().setShowLog(true, "ws-client").build();
    }

    public static IIotClient getInstance(Context context, ExecutorService executorService,
            IAuthView authView, DeviceInfo deviceInfo) {
        synchronized (IotClient.class) {
            if (instance == null) {
                instance = new IotClient(context, executorService, authView, deviceInfo);
            }
            return instance;
        }
    }

    @Override
    public void work() {
        logger.d("work");
        this.executorService.execute(() -> {

            String webSocketUrl = String.format("%s?%s", BuildConfig.REGISTER_HOST,
                    deviceInfo.toString().replaceAll(",", "&"));
            this.client.connect(webSocketUrl, new IConnectCallback() {
                @Override public void onConnected() {
                    authView.onShowAuthView("");
                    logger.d("onConnected");
                }

                @Override
                public void onReConnect() {
                    logger.d("onReConnect");
                }

                @Override public void onConnectFailed(Throwable e) {
                    logger.d("onConnectFailed "+e.getMessage());
                }

                @Override public void onDisconnect() {
                    logger.d("onDisconnect");
                }

                @Override public void onMessage(String message) {
                    logger.d("onMessage: "+message);
                }

                @Override public void onMessage(ByteBuffer bytes) {
                    logger.d("connect success");
                }
            });
        });
    }
}
