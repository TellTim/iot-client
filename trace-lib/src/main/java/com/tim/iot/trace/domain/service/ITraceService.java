package com.tim.iot.trace.domain.service;

import com.tim.iot.trace.domain.aggregate.TraceEntity;

/**
 * ITraceService
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:33
 */
public interface ITraceService {
    void sendTrace(TraceEntity entity);
}
