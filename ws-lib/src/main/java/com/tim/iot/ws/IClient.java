package com.tim.iot.ws;

import java.util.concurrent.TimeUnit;

/**
 * IClient
 *
 * @author Tell.Tim
 * @date 2019/12/24 9:53
 */
public interface IClient {

    void connect(String url,IConnectCallback connectCallback);

    void close(String url, ICallBack<Void> closeHandle);

    void send(String url,String simpleText,ICallBack sendCallback);
}
