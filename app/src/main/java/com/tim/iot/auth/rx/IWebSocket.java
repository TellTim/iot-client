package com.tim.iot.auth.rx;

import com.tim.iot.auth.entity.WebSocketInfo;
import io.reactivex.Observable;

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:35
 */
public interface IWebSocket {
    /**
     * 获取连接，并返回可观察对象WebSocketInfo
     */
    Observable<WebSocketInfo> connect(String url, int timeoutOfSecond);

    /**
     * 马上关闭所有连接
     */
    void closeConnect(String url);
}
