package com.tim.iot.register.api;

import com.tim.iot.register.protocol.QrCode;
import com.tim.iot.register.protocol.Register;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * IRegisterApi
 *
 * @author Tell.Tim
 * @date 2019/12/27 19:09
 */
public interface IRegisterApi {

        @POST("register")
        Observable<Register.Result> register(@Body Register.Param param);
        @POST("qrcode")
        Observable<QrCode.Result> qrCode(@Body QrCode.Param param);
}
