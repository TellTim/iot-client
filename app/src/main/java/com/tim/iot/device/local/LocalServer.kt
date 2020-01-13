package com.tim.iot.device.local

import android.content.Context
import android.content.SharedPreferences
import com.tim.android.constant.AppConst

/**
 * LocalServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:55
 */
class LocalServer(private val context: Context) : ILocalServer {

    override val account: String
        @Synchronized get() {
            val sharedPreferences =
                this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE)
            val authValue = sharedPreferences.getString(AppConst.AUTH_ACCOUNT_ITEM,
                    AppConst.UN_AUTH_ACCOUNT_VALUE)
            return if (authValue == AppConst.UN_AUTH_ACCOUNT_VALUE) {
                "Empty account"
            } else {
                authValue!!.substring(0, authValue.indexOf("&"))
            }
        }

    @Synchronized override fun saveAuthToLocal(accountInfo: String) {
        val sharedPreferences =
            this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(AppConst.AUTH_ACCOUNT_ITEM, accountInfo).apply()
    }

    @Synchronized override fun clearAuthorized() {
        //恢复默认的标记
        val sharedPreferences =
            this.context.getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE)
        if (sharedPreferences.getString(AppConst.AUTH_ACCOUNT_ITEM,
                        AppConst.UN_AUTH_ACCOUNT_VALUE) != AppConst.UN_AUTH_ACCOUNT_VALUE
        ) {
            sharedPreferences.edit()
                    .putString(AppConst.AUTH_ACCOUNT_ITEM, AppConst.UN_AUTH_ACCOUNT_VALUE)
                    .apply()
        }
    }
}
