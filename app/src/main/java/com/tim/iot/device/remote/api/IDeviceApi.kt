package com.tim.iot.device.remote.api

import com.tim.iot.device.remote.protocol.QrCode
import com.tim.iot.device.remote.protocol.Register
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * IDeviceApi
 *
 * @author Tell.Tim
 * @date 2019/12/27 19:09
 */
interface IDeviceApi {

    @POST("register")
    fun register(@Body param: Register.Param): Observable<Register.Result>

    @POST("qrcode")
    fun qrCode(@Body param: QrCode.Param): Observable<QrCode.Result>
}
