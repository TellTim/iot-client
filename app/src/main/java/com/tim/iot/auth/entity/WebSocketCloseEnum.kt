package com.tim.iot.auth.entity

/**
 * @author Tell.Tim
 * @date 2019/12/3 14:36
 */
enum class WebSocketCloseEnum private constructor(var code: Int, var reason: String?) {
    /**
     * websocket关闭枚举
     */
    USER_EXIT(1000, "close"),
    FORCE_EXIT(3000, "timeout close")
}
