package com.tim.common

/**
 * Respond
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:48
 */
class Respond<T>(var state: State?, var t: T?) {

    enum class State private constructor(var code: String?, var value: String?) {
        /**
         *
         */
        SUCCESS("200", "success"),
        PARAM_EMPTY("3000", "param empty"),
        BIND_EXIST("4000", "bind exist"),
        BIND_NOT_EXIST("4001", "bind not exist"),
        LOCAL_BIND_NOT_EXIST("4002", "local bind not exist"),
        DEVICE_NOT_EXIST("5000", "device not exist"),
        REGISTER_FAILURE("5001", "device not exist"),
        ERROR("9000", "error"),
        NET_ERROR("9001", "net error"),
        TYPE_INVALID("9002", "type not support")
    }
}
