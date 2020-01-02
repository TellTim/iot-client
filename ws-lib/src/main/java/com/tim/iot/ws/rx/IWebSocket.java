package com.tim.iot.ws.rx;

import com.tim.iot.ws.entity.WebSocketInfo;
import io.reactivex.Observable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okio.ByteString;

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:35
 */
public interface IWebSocket {
    /**
     * 获取连接，并返回观察对象
     */
    Observable<WebSocketInfo> connect(String url,int timeoutOfSecond);

    void send(String url, String msg);

    void send(String url, ByteString msg);

    /**
     * 马上关闭指定Url的连接
     */
    void closeNow(String url);

    /**
     * 马上关闭所有连接
     */
    void closeAllNow();
}
