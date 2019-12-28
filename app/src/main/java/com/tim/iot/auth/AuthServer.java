package com.tim.iot.auth;

import android.content.Context;
import com.tim.common.Logger;
import com.tim.iot.BuildConfig;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.local.ILocalServer;
import com.tim.iot.register.IRegisterServer;
import com.tim.iot.ws.Builder;
import com.tim.iot.ws.IClient;
import com.tim.iot.ws.WebSocketListener;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

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
    public AuthServer( DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        this.client = new Builder().setShowLog(true, "AuthServer-client").build();
    }

    @Override
    public void work() {
        logger.d("work");
    }
}
