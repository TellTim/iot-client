package com.tim.iot.local;

import com.tim.common.ICallback;
import com.tim.common.Respond;

/**
 * ILocalServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 11:09
 */
public interface ILocalServer {

    void checkAuthFromLocal(ICallback<String, Respond> callBack);

    void saveAuthToLocal(String account);
}
