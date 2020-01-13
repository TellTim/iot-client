package com.tim.iot.auth

import com.tim.iot.auth.rx.impl.RxWebSocket
import io.reactivex.annotations.NonNull
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:14
 */
class Builder {
    private var reconnectInterval = 1500L
    private var reconnectIntervalTimeUnit = TimeUnit.MILLISECONDS
    private var showLog = false
    private var logTag = "client"
    private val client: OkHttpClient
    private var sslSocketFactory: SSLSocketFactory? = null
    private var trustManager: X509TrustManager? = null
    private var enableHeartBeat: Boolean = false
    private var heartBeatHead: String? = null

    init {
        try {
            Class.forName("okhttp3.OkHttpClient")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Must be dependency okhttp3 !")
        }

        try {
            Class.forName("okhttp3.logging.HttpLoggingInterceptor")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Must be dependency okhttp3.logging !")
        }

        try {
            Class.forName("io.reactivex.Observable")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Must be dependency rxjava 2.x")
        }

        try {
            Class.forName("io.reactivex.android.schedulers.AndroidSchedulers")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Must be dependency rxandroid 2.x")
        }

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS
        this.client = OkHttpClient.Builder().addInterceptor(logging).build()
    }

    fun setShowLog(showLog: Boolean): Builder {
        this.showLog = showLog
        return this
    }

    fun setShowLog(showLog: Boolean, logTag: String): Builder {
        setShowLog(showLog)
        this.logTag = logTag
        return this
    }

    fun setHeartBeat(enable: Boolean): Builder {
        this.enableHeartBeat = enable
        this.heartBeatHead = "hc"
        return this
    }

    fun setHeartBeat(enable: Boolean, head: String): Builder {
        setHeartBeat(enable)
        this.heartBeatHead = head
        return this
    }

    ///外部不需要设置http客户端,暂时不支持ping的功能(容易触发异常,不受控制)
    /*public Builder setClient(@NonNull OkHttpClient client) {
        this.client = client;
        return this;
    }*/

    fun sslSocketFactory(@NonNull sslSocketFactory: SSLSocketFactory,
        trustManager: X509TrustManager): Builder {
        this.sslSocketFactory = sslSocketFactory
        this.trustManager = trustManager
        return this
    }

    fun reconnectInterval(reconnectInterval: Long,
        reconnectIntervalTimeUnit: TimeUnit): Builder {
        this.reconnectInterval = reconnectInterval
        this.reconnectIntervalTimeUnit = reconnectIntervalTimeUnit
        return this
    }

    fun build(): RxWebSocket {
        return RxWebSocket(this.client,
                this.reconnectInterval, this.reconnectIntervalTimeUnit,
                this.showLog, this.logTag,
                this.sslSocketFactory, this.trustManager, this.enableHeartBeat, this.heartBeatHead)
    }
}
