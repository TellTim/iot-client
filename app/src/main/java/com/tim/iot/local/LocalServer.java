package com.tim.iot.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.tim.android.constant.AppConst;
import com.tim.common.ICallback;
import com.tim.common.Respond;

/**
 * LocalServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:55
 */
public class LocalServer implements ILocalServer {

    private Context context;

    public LocalServer(Context context) {
        this.context = context;
    }

    @Override
    public void checkAuthFromLocal(ICallback<String, Respond> callBack) {
        SharedPreferences sharedPreferences =
                this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE);
        String authValue = sharedPreferences.getString(AppConst.AUTH_ACCOUNT_ITEM, "");
        if (TextUtils.isEmpty(authValue) || authValue.equals(AppConst.UN_AUTH_ACCOUNT_VALUE)) {
            callBack.onFail(new Respond(Respond.State.LOCAL_BIND_NOT_EXIST,Respond.State.LOCAL_BIND_NOT_EXIST.getValue()));
        } else {
            String authAccount = authValue.substring(0, authValue.indexOf("&"));
            callBack.onSuccess(authAccount);
        }
    }

    @Override
    public void saveAuthToLocal(String account) {
        SharedPreferences sharedPreferences =
                this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(AppConst.AUTH_ACCOUNT_ITEM,account).apply();
    }
}
