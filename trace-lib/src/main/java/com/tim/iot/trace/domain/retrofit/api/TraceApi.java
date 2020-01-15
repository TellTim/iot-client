package com.tim.iot.trace.domain.retrofit.api;

import com.tim.iot.trace.domain.retrofit.protocol.Trace;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * TraceApi
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:49
 */
public interface TraceApi {

    @POST("api/v1/trace")
    Observable<Trace.Result> sendTrace(@Body Trace.Param param);
}
