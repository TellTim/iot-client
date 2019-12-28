package com.tim.iot.common;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

/**
 * AccountInfo
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:54
 */
public class AccountInfo {
    @SerializedName("account")
    private String account;
    @SerializedName("createAt")
    private Long createAt;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s&%s", this.account, this.createAt.toString());
    }
}
