package com.tim.common

/**
 * ICallback
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:44
 */
interface ICallback<S, F> {
    fun onSuccess(s: S)
    fun onFail(f: F)
    fun onError(throwable: Throwable)
}
