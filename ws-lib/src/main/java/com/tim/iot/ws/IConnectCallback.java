package com.tim.iot.ws;

import java.nio.ByteBuffer;

/**
 * @author Tell.Tim
 * @date 2019/12/5 12:17
 */
public interface IConnectCallback {
    void onConnected();

    void onReConnect();

    void onConnectFailed(Throwable e);

    void onDisconnect();

    void onMessage(String message);

    void onMessage(ByteBuffer bytes);
}
