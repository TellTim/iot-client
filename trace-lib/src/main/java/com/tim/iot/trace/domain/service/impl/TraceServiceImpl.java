package com.tim.iot.trace.domain.service.impl;

import com.google.gson.GsonBuilder;
import com.tim.iot.trace.domain.aggregate.TraceEntity;
import com.tim.iot.trace.domain.entity.AppInfo;
import com.tim.iot.trace.domain.entity.TraceInfo;
import com.tim.iot.trace.domain.retrofit.api.TraceApi;
import com.tim.iot.trace.domain.retrofit.protocol.Trace;
import com.tim.iot.trace.domain.service.ITraceService;
import com.tim.iot.trace.util.Logger;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * TraceServiceImpl
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:47
 */
public class TraceServiceImpl implements ITraceService {
    private static final Logger logger = Logger.getLogger("service");
    private TraceApi traceApi;
    private AppInfo appInfo;

    public TraceServiceImpl(String traceHost, AppInfo appInfo) {
        this.appInfo = appInfo;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        //}
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(traceHost)
                .addConverterFactory(
                        GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
        this.traceApi = retrofit.create(TraceApi.class);
    }

    @Override
    public void sendTrace(TraceEntity entity) {
        Trace.Param param = new Trace.Param();
        TraceInfo traceInfo = entity.getTraceInfo();
        param.setTraceClass(traceInfo.getClassName());
        param.setTraceFunction(traceInfo.getFunctionName());
        param.setTraceInfo(traceInfo.getInfo());
        param.setTraceFrom(appInfo.getAppVersion());
        param.setTraceDeviceId(appInfo.getDeviceId());
        param.setTraceType(traceInfo.getType());
        param.setTimestamp(System.currentTimeMillis());
        this.traceApi.sendTrace(param)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single()).subscribe(new Observer<Trace.Result>() {
            @Override public void onSubscribe(Disposable d) {

            }

            @Override public void onNext(Trace.Result result) {
                if (result.getCode().equals("200")) {
                    logger.d("send success");
                }
            }

            @Override
            public void onError(Throwable e) {
                logger.e("send error " + e.getMessage());
            }

            @Override
            public void onComplete() {
                logger.d("send onComplete");
            }
        });
    }
}
