package com.tim.iot.ws;

import android.util.Log;
import com.tim.iot.ws.entity.WebSocketInfo;
import com.tim.iot.ws.rx.IWebSocket;
import com.tim.iot.ws.rx.impl.RxWebSocket;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * EasyClient
 *
 * @author Tell.Tim
 * @date 2019/12/24 9:53
 */
public class EasyClient implements IClient {
    private IWebSocket webSocket;
    private boolean showLog;
    private String logTag;

    public EasyClient(Builder builder) {
        this.showLog = builder.showLog;
        this.logTag = builder.logTag;
        this.webSocket = new RxWebSocket(builder.client,
                builder.reconnectInterval, builder.reconnectIntervalTimeUnit,
                builder.showLog, builder.logTag,
                builder.sslSocketFactory, builder.trustManager);
    }

    @Override
    public void connect(String url,final int timeoutOfSecond, WebSocketListener connectHandle) {
        webSocket.connect(url,timeoutOfSecond).subscribe(new Observer<WebSocketInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (showLog) {
                            Log.d(logTag, " --> onSubscribe " + d.toString());
                        }
                    }

                    @Override
                    public void onNext(WebSocketInfo webSocketInfo) {
                        if (webSocketInfo.isConnected()) {
                            connectHandle.onConnected();
                        } else if (webSocketInfo.getSimpleMsg() != null) {
                            connectHandle.onMessage(webSocketInfo.getSimpleMsg());
                        } else if (webSocketInfo.getByteStringMsg() != null) {
                            connectHandle.onMessage(webSocketInfo.getSimpleMsg());
                        } else if (webSocketInfo.isReconnect()) {
                            connectHandle.onReConnect();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        connectHandle.onConnectFailed(e);
                    }

                    @Override
                    public void onComplete() {
                        if (showLog) {
                            Log.d(logTag, " -->onComplete ");
                        }
                    }
                });
    }

    @Override
    public void close(String url, ICallback<Void> closeHandle) {
        Disposable disposable = this.webSocket.connect(url,120).subscribe();
        if (!disposable.isDisposed()) {
            disposable.dispose();
            closeHandle.onSuccess(null);
        } else {
            closeHandle.onFailure("未连接,无法断开");
        }
    }

    @Override
    public void send(String url, String simpleText, ICallback sendCallback) {

    }

    @Override
    public void closeAll() {
        this.webSocket.closeAllNow();
    }
}
