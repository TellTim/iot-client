package com.tim.common;

/**
 * ICallback
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:44
 */
public interface ICallback<S, F>{
    void onSuccess(S s);
    void onFail(F f);
    void onError(Throwable throwable);
}
