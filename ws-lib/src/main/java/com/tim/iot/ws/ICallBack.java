package com.tim.iot.ws;

/**
 * ICallBack
 *
 * @author Tell.Tim
 * @date 2019/12/24 12:52
 */
public interface ICallBack<T> {
    void onSuccess(T t);
    void onFailure(String msg);
    void onError(Throwable throwable);
}
