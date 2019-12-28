package com.tim.iot.ws;

/**
 * IClient
 *
 * @author Tell.Tim
 * @date 2019/12/24 9:53
 */
public interface IClient {

    void connect(String url, WebSocketListener connectCallback);

    void close(String url, ICallback<Void> closeHandle);

    void send(String url,String simpleText, ICallback sendCallback);
}
