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

    public TraceInfo(String className, String functionName, String info, String type) {
        this.className = className;
        this.functionName = functionName;
        this.info = info;
        this.type = type;
    }

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

}
