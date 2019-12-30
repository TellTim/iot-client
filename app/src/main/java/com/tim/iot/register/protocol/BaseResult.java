package com.tim.iot.register.protocol;

import com.google.gson.annotations.SerializedName;
import com.tim.iot.common.AccountInfo;

/**
 * BaseResult
 *
 * @author Tell.Tim
 * @date 2019/12/27 20:30
 */
public class BaseResult {
    private String code;
    private String data;
    @SerializedName("accountInfo")
    private AccountInfo accountInfo;

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
