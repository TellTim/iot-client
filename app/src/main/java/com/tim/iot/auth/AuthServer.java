package com.tim.iot.auth;

import android.text.TextUtils;
import com.tim.common.Logger;
import com.tim.iot.auth.entity.WebSocketInfo;
import com.tim.iot.auth.rx.IWebSocket;
import com.tim.iot.device.entity.AccountInfo;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * AuthServer
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:33
 */
public class AuthServer implements IAuthServer {
    private static final String TAG = "AuthServer";
    private static final Logger logger = Logger.getLogger(TAG);
    private IWebSocket webSocket;
    private Disposable disposable;
    public AuthServer() {
        this.webSocket = new Builder().setHeartBeat(true).setShowLog(true, TAG).build();
    }

    @Override
    public synchronized void connect(String url, int timeoutOfSecond,
            IConnectAuthServerCallback connectAuthServerCallback) {
        this.webSocket.connect(url, timeoutOfSecond).subscribe(new Observer<WebSocketInfo>() {
            @Override
            public void onSubscribe(Disposable d) {
                logger.d("订阅关系已建立");
                disposable = d;
            }

            @Override
            public void onNext(WebSocketInfo webSocketInfo) {
                if (webSocketInfo.isConnected()) {
                    logger.d("连接成功");
                    connectAuthServerCallback.onConnectSuccess();
                } else if (webSocketInfo.getSimpleMsg() != null) {
                    String simpleMessage = webSocketInfo.getSimpleMsg();
                    logger.d("收到消息: " + simpleMessage);
                    if (!TextUtils.isEmpty(simpleMessage) && simpleMessage.startsWith("confirm")) {
                        String[] convertArray = simpleMessage.split("#");
                        AccountInfo accountInfo = new AccountInfo();
                        accountInfo.setAccount(convertArray[1]);
                        accountInfo.setCreateAt(Long.valueOf(convertArray[2]));
                        connectAuthServerCallback.onConfirm(accountInfo);
                    }
                } else if (webSocketInfo.getByteStringMsg() != null) {
                    logger.d("receive byte message");
                } else if (webSocketInfo.isReconnect()) {
                    logger.d("正在重连");
                } else if (webSocketInfo.isPreConnect()) {
                    logger.d("准备连接");
                }
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof TimeoutException) {
                    logger.d("超时未响应");
                    connectAuthServerCallback.onTimeOut();
                } else {
                    logger.d("连接异常 " + e.getMessage());
                    connectAuthServerCallback.onConnectError(new Exception(e));
                }
            }

            @Override
            public void onComplete() {
                logger.d(" --> 处理完毕");
            }
        });
    }

    @Override
    public void closeConnect(String url) {
        logger.d("closeConnect");
        if (!disposable.isDisposed()) {
            logger.d("dispose");
            disposable.dispose();
        } else {
            logger.d("dispose isDisposed");
        }
    }

    @Override
    public void forceClose() {
        this.webSocket.forceClose();
    }
}