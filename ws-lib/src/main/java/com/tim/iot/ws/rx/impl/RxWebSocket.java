package com.tim.iot.ws.rx.impl;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import com.tim.iot.ws.BuildConfig;
import com.tim.iot.ws.entity.WebSocketCloseEnum;
import com.tim.iot.ws.entity.WebSocketInfo;
import com.tim.iot.ws.rx.IWebSocket;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:40
 */
public final class RxWebSocket implements IWebSocket {
    private static final long DEFAULT_TIMEOUT = 5L;
    private OkHttpClient mClient;
    private long mReconnectInterval;
    private TimeUnit mReconnectIntervalTimeUnit;
    private Map<String, Observable<WebSocketInfo>> observableMap;
    private Map<String, WebSocket> webSocketMap;
    private boolean showLog;
    private String logTag;
    private SSLSocketFactory mSslSocketFactory;
    private X509TrustManager mTrustManager;
    private static final int noAskTimeOut = 120;
    public RxWebSocket(OkHttpClient client, long reconnectInterval,
            TimeUnit reconnectIntervalTimeUnit, boolean showLog, String logTag,
            SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.mReconnectInterval = reconnectInterval;
        this.mReconnectIntervalTimeUnit = reconnectIntervalTimeUnit;
        this.showLog = showLog;
        this.logTag = logTag + "-lib";
        if (sslSocketFactory != null && trustManager != null) {
            this.mClient =
                    client.newBuilder().sslSocketFactory(sslSocketFactory, trustManager).build();
        } else {
            this.mClient = client;
        }
        this.observableMap = new ConcurrentHashMap<>();
        this.webSocketMap = new ConcurrentHashMap<>();
    }

    /**
     * 在指定时间间隔后没有收到消息就会重连WebSocket,
     *
     * @param url String
     * @return Observable<WebSocketInfo>
     */
    @Override
    public synchronized Observable<WebSocketInfo> connect(String url,final int timeoutOfSecond) {
        Observable<WebSocketInfo> observable = observableMap.get(url);
        if (observable == null) {
            observable = Observable.create(new WebSocketOnSubscribe(url))
                    //自动重连
                    .timeout(timeoutOfSecond, TimeUnit.SECONDS)
                    .retry(throwable -> {
                                if (showLog) {
                                    if (throwable instanceof TimeoutException) {
                                        Log.d(logTag, String.format(" --> %d %s 未出现反馈,网络可能已经中断,%d %s 后重连",
                                                timeoutOfSecond,TimeUnit.SECONDS.toString(),
                                                mReconnectInterval,mReconnectIntervalTimeUnit.toString() ));
                                    } else if (throwable instanceof IOException) {
                                        Log.e(logTag, String.format(" -->  网络出现异常，%d %s 后重连",
                                                mReconnectInterval,mReconnectIntervalTimeUnit.toString()));
                                    }
                                    Log.e(logTag, String.format(" -->  ###网络出现异常，%d %s:Error:%s",
                                            mReconnectInterval,mReconnectIntervalTimeUnit.toString(),throwable.toString()));
                                    if(throwable instanceof ProtocolException){
                                        return false;
                                    }
                                }
                                return throwable instanceof IOException
                                        || throwable instanceof TimeoutException;
                            }
                    )
                    .doOnDispose(() -> {
                        if (showLog) {
                            Log.d(logTag, url + " --> 订阅取消,断开连接");
                        }
                        closeNow(url);
                    })
                    .share()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            observableMap.put(url, observable);
            if (showLog) {
                Log.d(logTag, " --> 插入缓存成功 ");
            }
        } else {
            if (showLog) {
                Log.d(logTag, " --> 从缓存连接池中取出");
            }
            WebSocket webSocket = webSocketMap.get(url);
            if (webSocket != null) {
                observable = observable.startWith(WebSocketInfo.createConnected(webSocket));
            } else {
                if (showLog) {
                    Log.e(logTag, "从缓存获取为空");
                }
            }
        }
        return observable.observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void send(String url, String msg) {
        WebSocket webSocket = webSocketMap.get(url);
        if (webSocket != null) {
            webSocket.send(msg);
        } else {
            throw new IllegalStateException("The WebSocket not open");
        }
    }

    @Override
    public void send(String url, ByteString msg) {
        WebSocket webSocket = webSocketMap.get(url);
        if (webSocket != null) {
            webSocket.send(msg);
        } else {
            throw new IllegalStateException("The WebSocket not open");
        }
    }

    @Override
    public Observable<Boolean> asyncSend(String url, String msg) {
        return getWebSocket(url)
                .take(1)
                .map(webSocket -> webSocket.send(msg));
    }

    @Override
    public Observable<Boolean> asyncSend(String url, ByteString byteString) {
        return getWebSocket(url)
                .take(1)
                .map(webSocket -> webSocket.send(byteString));
    }

    @Override
    public Observable<Boolean> close(String url) {
        return Observable.create((ObservableOnSubscribe<WebSocket>) emitter -> {
            WebSocket webSocket = webSocketMap.get(url);
            if (webSocket == null) {
                emitter.onError(new NullPointerException(url + " --> close,关闭时,连接已不存在"));
            } else {
                emitter.onNext(webSocket);
            }
        }).map(this::closeWebSocket);
    }

    @Override
    public void closeNow(String url) {
        if (showLog) {
            Log.d(logTag, url + " --> 马上关闭连接");
        }
        closeWebSocket(webSocketMap.get(url));
    }

    @Override
    public Observable<List<Boolean>> closeAll() {
        if (showLog) {
            Log.d(logTag, "关闭所有连接");
        }
        return Observable
                .just(webSocketMap)
                .map(Map::values)
                .concatMap(
                        (Function<Collection<WebSocket>, ObservableSource<WebSocket>>) Observable::fromIterable)
                .map(
                        this::closeWebSocket)
                .collect(
                        (Callable<List<Boolean>>) ArrayList::new,
                        List::add)
                .toObservable();
    }

    @Override
    public void closeAllNow() {
        for (Map.Entry<String, WebSocket> entry : webSocketMap.entrySet()) {
            closeWebSocket(entry.getValue());
        }
    }

    private final class WebSocketOnSubscribe implements ObservableOnSubscribe<WebSocketInfo> {
        private String mWebSocketUrl;
        private WebSocket mWebSocket;

        WebSocketOnSubscribe(String url) {
            this.mWebSocketUrl = url;
        }

        @Override
        public void subscribe(ObservableEmitter<WebSocketInfo> emitter) {
            if (mWebSocket != null) {
                if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                    long millis = mReconnectIntervalTimeUnit.toMillis(mReconnectInterval);
                    if (millis == 0) {
                        millis = DEFAULT_TIMEOUT * 1000;
                    }
                    if (showLog) {
                        Log.d(logTag, String.format(" --> %d秒后即将重连",millis));
                    }
                    SystemClock.sleep(millis);
                    emitter.onNext(WebSocketInfo.createReconnect());
                }
            }
            if (showLog) {
                Log.d(logTag, "WebSocketOnSubscribe");
            }
            initWebSocket(emitter);
        }

        private void initWebSocket(ObservableEmitter<WebSocketInfo> emitter) {
            mWebSocket = mClient.newWebSocket(getRequest(mWebSocketUrl), new WebSocketListener() {
                @Override
                public void onOpen(final WebSocket webSocket, Response response) {
                    webSocketMap.put(mWebSocketUrl, webSocket);
                    if (showLog) {
                        Log.d(logTag,String.format("%s --> onOpen,新连接插入连接池中,连接池中包含%d个连接",mWebSocketUrl,webSocketMap.size()));
                    }
                    if (!emitter.isDisposed()) {
                        if (showLog) {
                            Log.d(logTag, mWebSocketUrl + " --> 上报已经连接状态");
                        }
                        emitter.onNext(WebSocketInfo.createConnected(webSocket));
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    if (showLog) {
                        Log.d(logTag, mWebSocketUrl + " --> onMessage ,收到新消息:" + text);
                    }
                    if (!emitter.isDisposed()) {
                        if (showLog) {
                            Log.d(logTag, mWebSocketUrl + " --> 上报收到新消息" + text);
                        }
                        emitter.onNext(WebSocketInfo.createMsg(webSocket, text));
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    if (showLog) {
                        Log.d(logTag, mWebSocketUrl + " --> onMessage ,收到新消息:" + bytes.base64());
                    }
                    if (!emitter.isDisposed()) {
                        if (showLog) {
                            Log.d(logTag, mWebSocketUrl + " --> 上报收到新消息" + bytes.base64());
                        }
                        emitter.onNext(WebSocketInfo.createByteStringMsg(webSocket, bytes));
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    if (showLog) {
                        Log.e(logTag,String.format("%s --> onFailure %s %s",mWebSocketUrl,t.toString(), webSocket.request().url().uri().getPath()));
                    }
                    if (!emitter.isDisposed()) {
                        if (showLog) {
                            Log.d(logTag, mWebSocketUrl + " --> 上报异常: " + t.toString());
                        }
                        emitter.onError(t);
                    }
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    if (showLog) {
                        Log.e(logTag, mWebSocketUrl
                                + " --> onClosing:code="
                                + code
                                + " reason="
                                + reason);
                    }
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    if (showLog) {
                        Log.d(logTag,
                                mWebSocketUrl + " --> onClosed:code=" + code + " reason=" + reason);
                    }
                }
            });
        }
    }

    private void removeWebSocketCache(WebSocket webSocket) {
        for (Map.Entry<String, WebSocket> entry : webSocketMap.entrySet()) {
            if (entry.getValue() == webSocket) {
                String url = entry.getKey();
                webSocketMap.remove(url);
            }
        }
    }

    private void removeUrlWebSocketMapping(WebSocket webSocket) {
        for (Map.Entry<String, WebSocket> entry : webSocketMap.entrySet()) {
            if (entry.getValue() == webSocket) {
                String url = entry.getKey();
                observableMap.remove(url);
                webSocketMap.remove(url);
            }
        }
    }

    private Observable<WebSocket> getWebSocket(String url) {
        return connect(url,noAskTimeOut)
                .map(WebSocketInfo::getWebSocket);
    }

    private boolean closeWebSocket(WebSocket webSocket) {
        if (webSocket == null) {
            if (showLog) {
                Log.e(logTag, "连接已不存在,关闭连接失败");
            }
            return false;
        }
        WebSocketCloseEnum normalCloseEnum = WebSocketCloseEnum.USER_EXIT;
        boolean result = webSocket.close(normalCloseEnum.getCode(), normalCloseEnum.getReason());
        if (result) {
            removeUrlWebSocketMapping(webSocket);
            if (showLog) {
                Log.d(logTag, "关闭连接成功");
            }
        } else {
            if (showLog) {
                Log.e(logTag, "关闭连接失败");
            }
        }
        return result;
    }

    private Request getRequest(String url) {
        return new Request.Builder().get().url(url).build();
    }
}
