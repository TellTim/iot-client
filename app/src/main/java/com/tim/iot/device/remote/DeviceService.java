package com.tim.iot.device.remote;

import com.google.gson.GsonBuilder;
import com.tim.common.ICallback;
import com.tim.common.Logger;
import com.tim.common.Respond;
import com.tim.iot.BuildConfig;
import com.tim.iot.device.entity.AccountInfo;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.device.remote.api.IDeviceApi;
import com.tim.iot.device.remote.protocol.QrCode;
import com.tim.iot.device.remote.protocol.Register;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.ExecutorService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * DeviceService
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:52
 */
public class DeviceService implements IDeviceServer {
    private static final String TAG = "DeviceService";
    private static final Logger logger = Logger.getLogger(TAG);
    private ExecutorService executorService;
    private IDeviceApi registerApi;

    public DeviceService(ExecutorService executorService) {
        this.executorService = executorService;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        //}
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.REGISTER_HOST)
                .addConverterFactory(
                        GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
        this.registerApi = retrofit.create(IDeviceApi.class);
    }

    @Override
    public void syncQrCode(DeviceInfo deviceInfo, ICallback<AccountInfo, Respond> callback) {
        QrCode.Param param = new QrCode.Param();
        param.setDeviceId(deviceInfo.getDeviceId());
        param.setType(deviceInfo.getType());
        param.setTimestamp(System.currentTimeMillis());
        this.registerApi.qrCode(param).subscribe(new Observer<QrCode.Result>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(QrCode.Result result) {
                if (Respond.State.BIND_EXIST.getCode().equals(result.getCode())) {
                    callback.onSuccess(result.getAccountInfo());
                } else if (Respond.State.BIND_NOT_EXIST.getCode().equals(result.getCode())) {
                    callback.onFail(new Respond<>(Respond.State.BIND_NOT_EXIST,
                            result.getQrCodeInfo()));
                } else if (Respond.State.TYPE_INVALID.getCode().equals(result.getCode())) {
                    callback.onFail(new Respond<>(Respond.State.TYPE_INVALID,
                            result.getCode() + " " + result.getData()));
                } else if (Respond.State.DEVICE_NOT_EXIST.getCode().equals(result.getCode())) {
                    callback.onFail(new Respond<>(Respond.State.DEVICE_NOT_EXIST,
                            result.getCode() + " " + result.getData()));
                } else {
                    callback.onFail(new Respond<>(Respond.State.ERROR,
                            result.getCode() + " " + result.getData()));
                }
            }

            @Override
            public void onError(Throwable e) {
                callback.onError(e);
            }

            @Override
            public void onComplete() {
                logger.d("同步二维码事件处理完毕");
            }
        });
    }

    @Override
    public void syncAuthorized(DeviceInfo deviceInfo, ICallback<AccountInfo, Respond> callback) {
        Register.Param param = new Register.Param();
        param.setDeviceId(deviceInfo.getDeviceId());
        param.setImei(deviceInfo.getImei());
        param.setMac(deviceInfo.getMac());
        param.setType(deviceInfo.getType());
        param.setTimestamp(System.currentTimeMillis());
        this.registerApi.register(param).subscribe(new Observer<Register.Result>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Register.Result result) {
                if (Respond.State.BIND_EXIST.getCode().equals(result.getCode())) {
                    callback.onSuccess(result.getAccountInfo());
                } else if (Respond.State.BIND_NOT_EXIST.getCode().equals(result.getCode())) {
                    callback.onFail(
                            new Respond<>(Respond.State.BIND_NOT_EXIST,
                                    Respond.State.BIND_NOT_EXIST.getValue()));
                } else {
                    callback.onFail(new Respond<>(Respond.State.ERROR,
                            result.getCode() + " " + result.getData()));
                }
            }

            @Override
            public void onError(Throwable e) {
                callback.onError(e);
            }

            @Override
            public void onComplete() {
            }
        });
    }
}
