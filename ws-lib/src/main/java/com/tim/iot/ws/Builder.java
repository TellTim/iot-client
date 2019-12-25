package com.tim.iot.ws;

import android.content.Context;
import io.reactivex.annotations.NonNull;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:14
 */
public final class Builder {
    long reconnectInterval = 3L;
    TimeUnit reconnectIntervalTimeUnit = TimeUnit.SECONDS;
    boolean showLog = false;
    String logTag = "client";
    OkHttpClient client;
    SSLSocketFactory sslSocketFactory;
    X509TrustManager trustManager;

    public Builder() {
        try {
            Class.forName("okhttp3.OkHttpClient");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency okhttp3 !");
        }
        try {
            Class.forName("okhttp3.logging.HttpLoggingInterceptor");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency okhttp3.logging !");
        }
        try {
            Class.forName("io.reactivex.Observable");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency rxjava 2.x");
        }
        try {
            Class.forName("io.reactivex.android.schedulers.AndroidSchedulers");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency rxandroid 2.x");
        }
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        this.client = new OkHttpClient.Builder().addInterceptor(logging).build();
    }

    public Builder setShowLog(boolean showLog) {
        this.showLog = showLog;
        return this;
    }

    public Builder setShowLog(boolean showLog, String logTag) {
        setShowLog(showLog);
        this.logTag = logTag;
        return this;
    }

    ///外部不需要设置http客户端,暂时不支持ping的功能(容易触发异常,不受控制)
    /*public Builder setClient(@NonNull OkHttpClient client) {
        this.client = client;
        return this;
    }*/

    public Builder sslSocketFactory(@NonNull SSLSocketFactory sslSocketFactory,
            X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
        return this;
    }

    public Builder reconnectInterval(long reconnectInterval,
            TimeUnit reconnectIntervalTimeUnit) {
        this.reconnectInterval = reconnectInterval;
        this.reconnectIntervalTimeUnit = reconnectIntervalTimeUnit;
        return this;
    }

    public EasyClient build() {
        return new EasyClient(this);
    }
}
