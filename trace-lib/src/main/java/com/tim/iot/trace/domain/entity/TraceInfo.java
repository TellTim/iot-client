package com.tim.iot.trace.domain.entity;

import androidx.annotation.NonNull;

/**
 * TraceInfo
 *
 * @author Tell.Tim
 * @date 2020/1/15 14:08
 */
public class TraceInfo {
    private static final int CLASS_NAME_MAX_LENGTH = 64;
    private static final int FUNCTION_NAME_MAX_LENGTH = 64;
    private static final int INFO_MAX_LENGTH = 128;
    private static final int TYPE_MAX_LENGTH = 64;
    private String className;
    private String functionName;
    private String info;
    private String type;

    public TraceInfo(@NonNull String className, @NonNull String functionName, @NonNull String info,
            @NonNull String type) {
        if (className.length() > CLASS_NAME_MAX_LENGTH) {
            this.className = className.substring(className.length() - CLASS_NAME_MAX_LENGTH);
        } else {
            this.className = className;
        }
        if (functionName.length() > FUNCTION_NAME_MAX_LENGTH) {
            this.functionName =
                    functionName.substring(functionName.length() - FUNCTION_NAME_MAX_LENGTH);
        } else {
            this.functionName = functionName;
        }
        if (info.length() > INFO_MAX_LENGTH) {
            this.info = info.substring(info.length() - INFO_MAX_LENGTH);
        } else {
            this.info = info;
        }
        if (type.length() > TYPE_MAX_LENGTH) {
            this.type = type.substring(type.length() - TYPE_MAX_LENGTH);
        } else {
            this.type = type;
        }
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
