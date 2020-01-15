package com.tim.iot.trace.domain.aggregate;

import com.tim.iot.trace.domain.entity.AppInfo;
import com.tim.iot.trace.domain.entity.TraceInfo;

/**
 * TraceEntity
 *
 * @author Tell.Tim
 * @date 2020/1/15 14:30
 */
public class TraceEntity {
    private AppInfo appInfo;
    private TraceInfo traceInfo;

    public TraceEntity(AppInfo appInfo, TraceInfo traceInfo) {
        this.appInfo = appInfo;
        this.traceInfo = traceInfo;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public void setTraceInfo(TraceInfo traceInfo) {
        this.traceInfo = traceInfo;
    }
}
