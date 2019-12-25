package com.tim.iot.ws.entity;

/**
 * @author Tell.Tim
 * @date 2019/12/3 14:36
 */
public enum WebSocketCloseEnum {
    /**
     * websocket关闭枚举
     */
    USER_EXIT(1000,"close");
    private int code;
    private String reason;

    WebSocketCloseEnum(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
