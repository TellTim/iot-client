package com.tim.iot.trace;

import com.tim.iot.trace.domain.entity.TraceInfo;

/**
 * ITraceClient
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:31
 */
public interface ITraceClient {

    void sendTrace(TraceInfo traceInfo);

    interface appInfoProvider {
        String applyAppVersion();
        String applyDeviceId();
        String applyTraceHost();
    }
}
