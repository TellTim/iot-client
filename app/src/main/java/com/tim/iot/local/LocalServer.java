package com.tim.iot.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.tim.android.constant.AppConst;

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
    public String getAccount() {
        SharedPreferences sharedPreferences =
                this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE);
        String authValue = sharedPreferences.getString(AppConst.AUTH_ACCOUNT_ITEM,
                AppConst.UN_AUTH_ACCOUNT_VALUE);
        if (authValue.equals(AppConst.UN_AUTH_ACCOUNT_VALUE)) {
            return "Empty account";
        } else {
            return authValue.substring(0, authValue.indexOf("&"));
        }
    }

    @Override
    public synchronized void saveAuthToLocal(String account) {
        SharedPreferences sharedPreferences =
                this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(AppConst.AUTH_ACCOUNT_ITEM, account).apply();
    }

    @Override
    public synchronized void clearAuthorized() {
        //恢复默认的标记
        SharedPreferences sharedPreferences =
                this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(AppConst.AUTH_ACCOUNT_ITEM, AppConst.UN_AUTH_ACCOUNT_VALUE)
                .apply();
    }
}
