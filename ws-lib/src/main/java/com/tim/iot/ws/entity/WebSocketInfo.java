package com.tim.iot.ws.entity;

import java.io.Serializable;
import okhttp3.WebSocket;
import okio.ByteString;

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:36
 */
public class WebSocketInfo implements Serializable {
    private static final long serialVersionUID = 1714523747523892210L;
    private WebSocket mWebSocket;
    private String simpleMsg;
    private ByteString byteStringMsg;
    private boolean connected;
    private boolean reconnect;

    public WebSocket getWebSocket() {
        return mWebSocket;
    }

    public void setWebSocket(WebSocket mWebSocket) {
        this.mWebSocket = mWebSocket;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    public String getSimpleMsg() {
        return simpleMsg;
    }

    public void setSimpleMsg(String simpleMsg) {
        this.simpleMsg = simpleMsg;
    }

    public ByteString getByteStringMsg() {
        return byteStringMsg;
    }

    public void setByteStringMsg(ByteString byteStringMsg) {
        this.byteStringMsg = byteStringMsg;
    }

    public WebSocketInfo reset() {
        this.mWebSocket = null;
        this.simpleMsg = null;
        this.byteStringMsg = null;
        this.connected = false;
        this.reconnect = false;
        return this;
    }

    public static WebSocketInfo createReconnect() {
        WebSocketInfo socketInfo = new WebSocketInfo();
        socketInfo.reconnect = true;
        return socketInfo;
    }

    public static WebSocketInfo createConnected(WebSocket webSocket) {
        WebSocketInfo socketInfo = new WebSocketInfo();
        socketInfo.connected = true;
        socketInfo.mWebSocket = webSocket;
        return socketInfo;
    }

    public static WebSocketInfo createMsg(WebSocket webSocket, String simpleMsg) {
        WebSocketInfo socketInfo = new WebSocketInfo();
        socketInfo.simpleMsg = simpleMsg;
        socketInfo.mWebSocket = webSocket;
        return socketInfo;
    }

    public static WebSocketInfo createByteStringMsg(WebSocket webSocket, ByteString byteMsg) {
        WebSocketInfo socketInfo = new WebSocketInfo();
        socketInfo.byteStringMsg = byteMsg;
        socketInfo.mWebSocket = webSocket;
        return socketInfo;
    }
}
