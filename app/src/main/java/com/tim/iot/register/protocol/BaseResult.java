package com.tim.iot.register.protocol;

/**
 * BaseResult
 *
 * @author Tell.Tim
 * @date 2019/12/27 20:30
 */
public class BaseResult {
    private String code;
    private String data;
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
