package com.tim.iot.device.remote.api;

import com.tim.iot.device.remote.protocol.QrCode;
import com.tim.iot.device.remote.protocol.Register;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * IDeviceApi
 *
 * @author Tell.Tim
 * @date 2019/12/27 19:09
 */
public interface IDeviceApi {

        @POST("register")
        Observable<Register.Result> register(@Body Register.Param param);
        @POST("qrcode")
        Observable<QrCode.Result> qrCode(@Body QrCode.Param param);
}
