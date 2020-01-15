package com.tim.iot.trace.domain.entity;

/**
 * TraceInfo
 *
 * @author Tell.Tim
 * @date 2020/1/15 14:08
 */
public class TraceInfo {
    private String className;
    private String functionName;
    private String info;
    private String type;
    private Long timestamp;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
