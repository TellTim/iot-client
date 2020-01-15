package com.tim.android;

import android.app.Application;
import com.tim.common.DeviceUtils;
import com.tim.iot.BuildConfig;
import com.tim.iot.trace.ITraceClient;
import com.tim.iot.trace.TraceClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IotApplication
 *
 * @author Tell.Tim
 * @date 2019/12/24 13:05
 */
public class IotApplication extends Application implements ITraceClient.AppInfoProvider {

    @Override
    public void onCreate() {
        super.onCreate();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        TraceClient.getInstance().init(executorService,this);
    }

    @Override
    public String applyAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String applyDeviceId() {
        return DeviceUtils.getDeviceSerial();
    }

    @Override
    public String applyTraceHost() {
        return BuildConfig.GATHER_HOST;
    }
}
