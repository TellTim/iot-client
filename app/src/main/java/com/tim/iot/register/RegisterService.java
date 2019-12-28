package com.tim.iot.register;

import com.google.gson.GsonBuilder;
import com.tim.common.ICallback;
import com.tim.common.Logger;
import com.tim.common.Respond;
import com.tim.iot.BuildConfig;
import com.tim.iot.common.AccountInfo;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.common.QrCodeInfo;
import com.tim.iot.register.api.IRegisterApi;
import com.tim.iot.register.protocol.Register;
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
 * RegisterService
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:52
 */
public class RegisterService implements IRegisterServer {
    private static final String TAG = "RegisterService";
    private static final Logger logger = Logger.getLogger(TAG);
    private ExecutorService executorService;
    private IRegisterApi registerApi;

    public RegisterService(ExecutorService executorService) {
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
        this.registerApi = retrofit.create(IRegisterApi.class);
    }

    @Override
    public void syncFromServer(DeviceInfo deviceInfo, ICallback<AccountInfo, Respond> callback) {
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
                    callback.onFail(new Respond<QrCodeInfo>(Respond.State.BIND_NOT_EXIST, result.getQrCodeInfo()));
                } else {
                    callback.onFail(new Respond<String>(Respond.State.ERROR, result.getCode()));
                    logger.e("unSupport response code"+result.getCode());
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
