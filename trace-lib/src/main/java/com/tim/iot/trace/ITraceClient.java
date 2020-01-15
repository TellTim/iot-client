package com.tim.iot.trace;

import com.tim.iot.trace.domain.entity.TraceInfo;
import java.util.concurrent.ExecutorService;

/**
 * ITraceClient
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:31
 */
public interface ITraceClient {
    void init(ExecutorService executorService,
            ITraceClient.AppInfoProvider appInfoProvider);
    void sendTrace(TraceInfo traceInfo);

    interface AppInfoProvider {
        String applyAppVersion();
        String applyDeviceId();
        String applyTraceHost();
    }
}
