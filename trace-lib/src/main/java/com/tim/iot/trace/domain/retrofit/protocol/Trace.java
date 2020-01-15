package com.tim.iot.trace.domain.retrofit.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * TraceEntity
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:54
 */
public class Trace {

    public static class Param{
        @SerializedName("traceDeviceId")
        private String traceDeviceId;
        @SerializedName("traceClass")
        private String traceClass;
        @SerializedName("traceFunction")
        private String traceFunction;
        @SerializedName("traceInfo")
        private String traceInfo;
        @SerializedName("traceType")
        private String traceType;
        @SerializedName("traceFrom")
        private String traceFrom;
        @SerializedName("timestamp")
        private Long timestamp;

        public String getTraceDeviceId() {
            return traceDeviceId;
        }

        public void setTraceDeviceId(String traceDeviceId) {
            this.traceDeviceId = traceDeviceId;
        }

        public String getTraceClass() {
            return traceClass;
        }

        public void setTraceClass(String traceClass) {
            this.traceClass = traceClass;
        }

        public String getTraceFunction() {
            return traceFunction;
        }

        public void setTraceFunction(String traceFunction) {
            this.traceFunction = traceFunction;
        }

        public String getTraceInfo() {
            return traceInfo;
        }

        public void setTraceInfo(String traceInfo) {
            this.traceInfo = traceInfo;
        }

        public String getTraceType() {
            return traceType;
        }

        public void setTraceType(String traceType) {
            this.traceType = traceType;
        }

        public String getTraceFrom() {
            return traceFrom;
        }

        public void setTraceFrom(String traceFrom) {
            this.traceFrom = traceFrom;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }


    public static class Result{
        private String code;
        private String data;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }


}
