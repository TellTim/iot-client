package com.tim.iot.local.entity;

/**
 * AccountInfo
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:54
 */
public class AccountInfo {
    private String account;
    private long createAt;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }
}
