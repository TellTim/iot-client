package com.tim.iot.trace;

import com.tim.iot.trace.domain.aggregate.TraceEntity;
import com.tim.iot.trace.domain.entity.AppInfo;
import com.tim.iot.trace.domain.entity.TraceInfo;
import com.tim.iot.trace.domain.service.ITraceService;
import com.tim.iot.trace.domain.service.impl.TraceServiceImpl;
import com.tim.iot.trace.util.DeviceUtil;
import java.util.concurrent.ExecutorService;

/**
 * TraceClient
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:32
 */
public class TraceClient implements ITraceClient {
    private ExecutorService executorService;
    private ITraceService traceServer;
    private AppInfo appInfo;

    public static TraceClient getInstance() {
        return TraceClientHolder.INSTANCE;
    }

    @Override
    public void init(ExecutorService executorService,
            ITraceClient.AppInfoProvider appInfoProvider) {
        if (this.executorService==null) {
            this.executorService = executorService;
        }
        if (this.appInfo==null) {
            this.appInfo =
                    new AppInfo(String.format("%s#%s", DeviceUtil.getSystemVersion(),
                            appInfoProvider.applyAppVersion()),
                            appInfoProvider.applyDeviceId());
        }
        if (this.traceServer==null) {
            this.traceServer = new TraceServiceImpl(appInfoProvider.applyTraceHost(), appInfo);
        }
    }



    @Override
    public void sendTrace(TraceInfo traceInfo) {
        if (this.executorService!=null) {
            this.executorService.execute(() -> {
                TraceEntity traceEntity = new TraceEntity(appInfo, traceInfo);
                if (traceServer!=null) {
                    traceServer.sendTrace(traceEntity);
                }
            });
        }
    }

    private static class TraceClientHolder {
        private static final TraceClient INSTANCE = new TraceClient();
    }
}
