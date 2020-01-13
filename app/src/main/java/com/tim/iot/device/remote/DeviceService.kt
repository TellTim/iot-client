package com.tim.iot.device.remote

import com.google.gson.GsonBuilder
import com.tim.common.ICallback
import com.tim.common.Logger
import com.tim.common.Respond
import com.tim.iot.BuildConfig
import com.tim.iot.device.entity.AccountInfo
import com.tim.iot.common.DeviceInfo
import com.tim.iot.device.entity.QrCodeInfo
import com.tim.iot.device.remote.api.IDeviceApi
import com.tim.iot.device.remote.protocol.QrCode
import com.tim.iot.device.remote.protocol.Register
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ExecutorService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * DeviceService
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:52
 */
class DeviceService(private val executorService: ExecutorService) : IDeviceServer {
    private val registerApi: IDeviceApi

    init {
        val builder = OkHttpClient.Builder()
        //if (BuildConfig.DEBUG) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(interceptor)
        //}
        val client = builder.build()
        val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.REGISTER_HOST)
                .addConverterFactory(
                        GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        this.registerApi = retrofit.create(IDeviceApi::class.java)
    }

    override fun syncQrCode(deviceInfo: DeviceInfo, callback: ICallback<AccountInfo, Respond<*>>) {
        val param = QrCode.Param()
        param.deviceId = deviceInfo.deviceId
        param.type = deviceInfo.type
        param.timestamp = System.currentTimeMillis()
        this.registerApi.qrCode(param).subscribe(object : Observer<QrCode.Result> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(result: QrCode.Result) {
                when {
                    Respond.State.BIND_EXIST.code == result.code -> result.accountInfo?.let { callback.onSuccess(it) }
                    Respond.State.BIND_NOT_EXIST.code == result.code -> callback.onFail(Respond<QrCodeInfo>(Respond.State.BIND_NOT_EXIST,
                            result.qrCodeInfo))
                    Respond.State.TYPE_INVALID.code == result.code -> callback.onFail(Respond(Respond.State.TYPE_INVALID,
                            result.code + " " + result.data))
                    Respond.State.DEVICE_NOT_EXIST.code == result.code -> callback.onFail(Respond(Respond.State.DEVICE_NOT_EXIST,
                            result.code + " " + result.data))
                    else -> callback.onFail(Respond(Respond.State.ERROR,
                            result.code + " " + result.data))
                }
            }

            override fun onError(e: Throwable) {
                callback.onError(e)
            }

            override fun onComplete() {
                logger.d("同步二维码事件处理完毕")
            }
        })
    }

    override fun syncAuthorized(deviceInfo: DeviceInfo,
        callback: ICallback<AccountInfo, Respond<*>>) {
        val param = Register.Param()
        param.deviceId = deviceInfo.deviceId
        param.imei = deviceInfo.imei
        param.mac = deviceInfo.mac
        param.type = deviceInfo.type
        param.timestamp = System.currentTimeMillis()
        this.registerApi.register(param).subscribe(object : Observer<Register.Result> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(result: Register.Result) {
                when {
                    Respond.State.BIND_EXIST.code == result.code -> result.accountInfo?.let { callback.onSuccess(it) }
                    Respond.State.BIND_NOT_EXIST.code == result.code -> callback.onFail(
                            Respond(Respond.State.BIND_NOT_EXIST,
                                    Respond.State.BIND_NOT_EXIST.value))
                    else -> callback.onFail(Respond(Respond.State.ERROR,
                            result.code + " " + result.data))
                }
            }

            override fun onError(e: Throwable) {
                callback.onError(e)
            }

            override fun onComplete() {}
        })
    }

    companion object {
        private const val TAG = "DeviceService"
        private val logger = Logger.getLogger(TAG)
    }
}
