package com.tim.iot.auth.rx.impl;

import android.os.Looper;
import android.os.SystemClock;
import com.tim.common.Logger;
import com.tim.iot.auth.entity.WebSocketCloseEnum;
import com.tim.iot.auth.entity.WebSocketInfo;
import com.tim.iot.auth.rx.IWebSocket;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Map;
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
    private Map<String, Observable<WebSocketInfo>> observableWebSocketInfoMap;
    private Map<String, WebSocket> webSocketMap;
    private boolean showLog;
    private SSLSocketFactory mSslSocketFactory;
    private X509TrustManager mTrustManager;
    private Logger logger;

    public RxWebSocket(OkHttpClient client, long reconnectInterval,
            TimeUnit reconnectIntervalTimeUnit, boolean showLog, String logTag,
            SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.mReconnectInterval = reconnectInterval;
        this.mReconnectIntervalTimeUnit = reconnectIntervalTimeUnit;
        this.showLog = showLog;
        this.logger = Logger.getLogger(logTag + "-client");
        if (sslSocketFactory != null && trustManager != null) {
            this.mClient =
                    client.newBuilder().sslSocketFactory(sslSocketFactory, trustManager).build();
        } else {
            this.mClient = client;
        }
        this.observableWebSocketInfoMap = new ConcurrentHashMap<>();
        this.webSocketMap = new ConcurrentHashMap<>();
    }

    /**
     * 当遇到网络波荡异常时，需要自动重新连接
     *
     * @param url String
     * @param timeoutOfSecond int
     * @return Observable<WebSocketInfo>
     */
    @Override
    public Observable<WebSocketInfo> connect(final String url, int timeoutOfSecond) {
        Observable<WebSocketInfo> observable = observableWebSocketInfoMap.get(url);
        if (observable == null) {
            observable = Observable.create(new WebSocketOnSubscribe(url))
                    //超时设置
                    .timeout(timeoutOfSecond, TimeUnit.SECONDS)
                    //重连
                    .retry(throwable -> {
                                if (showLog) {
                                    if (throwable instanceof TimeoutException) {
                                        logger.dFormat(" --> %d %s 超时未反馈,断开连接",
                                                timeoutOfSecond, TimeUnit.SECONDS.toString());
                                        return false;
                                    } else if (throwable instanceof ProtocolException) {
                                        logger.eFormat(" --> 网络交互异常: %s",
                                                throwable.toString());
                                        return false;
                                    } else if (throwable instanceof IOException) {
                                        logger.eFormat(" -->  网络出现异常，%d %s 后重连",
                                                mReconnectInterval,
                                                mReconnectIntervalTimeUnit.toString());
                                    }
                                }
                                return throwable instanceof IOException;
                            }
                    )
                    .doOnDispose(() -> {
                        if (showLog) {
                            logger.d("订阅取消,断开连接");
                        }
                        closeNow(url);
                    })
                    .share()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

            observableWebSocketInfoMap.put(url, observable);
            if (showLog) {
                logger.d("插入缓存成功 ");
            }
        } else {
            if (showLog) {
                logger.d("从缓存连接池中取出");
            }
            WebSocket webSocket = webSocketMap.get(url);
            if (webSocket != null) {
                observable = observable.startWith(WebSocketInfo.createConnected(webSocket));
            } else {
                if (showLog) {
                    logger.e("从缓存获取为空");
                }
            }
        }
        return observable.observeOn(AndroidSchedulers.mainThread());
    }

    private void closeNow(String url) {
        if (showLog) {
            logger.d("closeNow");
        }
        closeWebSocket(webSocketMap.get(url));
    }

    private void closeWebSocket(WebSocket webSocket) {
        if (webSocket == null) {
            if (showLog) {
                logger.dFormat(" --> 连接已不存在,缓冲池中还剩下%d个监听", observableWebSocketInfoMap.size());
            }
            return;
        }
        WebSocketCloseEnum normalCloseEnum = WebSocketCloseEnum.USER_EXIT;
        boolean result = webSocket.close(normalCloseEnum.getCode(), normalCloseEnum.getReason());
        if (result) {
            removeUrlWebSocketMapping(webSocket);
            if (showLog) {
                logger.dFormat(" --> 关闭连接成功,缓存池中还剩下%d个监听,%d个连接实例", webSocketMap.size(),
                        observableWebSocketInfoMap.size());
            }
        } else {
            if (showLog) {
                logger.e("关闭连接失败");
            }
        }
    }

    private void removeUrlWebSocketMapping(WebSocket webSocket) {
        if (showLog) {
            logger.d("removeUrlWebSocketMapping");
        }
        for (Map.Entry<String, WebSocket> entry : webSocketMap.entrySet()) {
            if (entry.getValue() == webSocket) {
                String url = entry.getKey();
                observableWebSocketInfoMap.remove(url);
                webSocketMap.remove(url);
            }
        }
    }

    @Override
    public void closeConnect(String url) {
        if (showLog) {
            logger.d("关闭连接");
        }
        Observable<WebSocketInfo> observable = observableWebSocketInfoMap.get(url);
        if (observable != null) {
            Disposable disposable = observable.subscribe();
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }else{
                if (showLog) {
                    logger.d("disposable.isDisposed");
                }
            }
        }else{
            if (showLog) {
                logger.d("observable == null");
            }
        }
    }

    private final class WebSocketOnSubscribe implements ObservableOnSubscribe<WebSocketInfo> {

        private String mWebSocketUrl;
        private WebSocket mWebSocket;

        WebSocketOnSubscribe(String url) {
            this.mWebSocketUrl = url;
        }

        @Override
        public void subscribe(ObservableEmitter<WebSocketInfo> emitter) throws Exception {
            if (showLog) {
                logger.d("subscribe");
            }
            if (mWebSocket != null) {
                if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                    long millis = mReconnectIntervalTimeUnit.toMillis(mReconnectInterval);
                    if (millis == 0) {
                        millis = DEFAULT_TIMEOUT * 1000;
                    }
                    if (showLog) {
                        logger.dFormat(" --> %d秒后即将重连", millis);
                    }
                    SystemClock.sleep(millis);
                    emitter.onNext(WebSocketInfo.createReconnect());
                }
            } else {
                mWebSocket =
                        mClient.newWebSocket(getRequest(mWebSocketUrl), new WebSocketListener() {
                            @Override
                            public void onOpen(final WebSocket webSocket, Response response) {
                                webSocketMap.put(mWebSocketUrl, webSocket);
                                if (showLog) {
                                    logger.dFormat(" --> %sonOpen,新连接插入连接池中", mWebSocketUrl);
                                }
                                if (!emitter.isDisposed()) {
                                    if (showLog) {
                                        logger.dFormat(" --> %s上报已经连接状态", mWebSocketUrl);
                                    }
                                    emitter.onNext(WebSocketInfo.createConnected(webSocket));
                                }
                            }

                            @Override
                            public void onMessage(WebSocket webSocket, String text) {
                                if (showLog) {
                                    logger.d("onMessage ,收到新消息:" + text);
                                }
                                if (!emitter.isDisposed()) {
                                    if (showLog) {
                                        logger.dFormat(" --> %s上报收到新消息: ", mWebSocketUrl, text);
                                    }
                                    emitter.onNext(WebSocketInfo.createMsg(webSocket, text));
                                }
                            }

                            @Override
                            public void onMessage(WebSocket webSocket, ByteString bytes) {
                                if (showLog) {
                                    logger.dFormat(" --> %sonMessage ,收到新消息: %s", mWebSocketUrl,
                                            bytes.base64());
                                }
                                if (!emitter.isDisposed()) {
                                    if (showLog) {
                                        logger.dFormat(" --> %sonMessage ,上报收到新消息: %s",
                                                mWebSocketUrl,
                                                bytes.base64());
                                    }
                                    emitter.onNext(
                                            WebSocketInfo.createByteStringMsg(webSocket, bytes));
                                }
                            }

                            @Override
                            public void onFailure(WebSocket webSocket, Throwable t,
                                    Response response) {
                                if (showLog) {
                                    logger.eFormat(" --> %sonFailure %s %s", mWebSocketUrl,
                                            t.toString(),
                                            webSocket.request().url().uri().getPath());
                                }
                                if (!emitter.isDisposed()) {
                                    if (showLog) {
                                        logger.eFormat(" --> %s上报异常: ", mWebSocketUrl,
                                                t.toString());
                                    }
                                    emitter.onError(t);
                                }
                            }

                            @Override
                            public void onClosing(WebSocket webSocket, int code, String reason) {
                                if (showLog) {
                                    logger.dFormat(
                                            "%sonClosing:code=%d,reason=%s", mWebSocketUrl,
                                            code, reason);
                                }
                            }

                            @Override
                            public void onClosed(WebSocket webSocket, int code, String reason) {
                                if (showLog) {
                                    logger.dFormat(
                                            "%sonClosed:code=%d,reason=%s", mWebSocketUrl,
                                            code, reason);
                                }
                            }
                        });
            }
        }
    }

    private Request getRequest(String url) {
        return new Request.Builder().get().url(url).build();
    }
}
