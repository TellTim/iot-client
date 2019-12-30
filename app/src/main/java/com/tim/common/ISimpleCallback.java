package com.tim.common;

/**
 * ISimpleCallback
 *
 * @author Tell.Tim
 * @date 2019/12/30 13:37
 */
public interface ISimpleCallback<S, F> {
    void onSuccess(S s);
    void onFail(F f);
}
