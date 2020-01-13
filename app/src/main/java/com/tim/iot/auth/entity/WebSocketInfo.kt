package com.tim.iot.auth.entity

import java.io.Serializable
import okhttp3.WebSocket
import okio.ByteString

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:36
 */
class WebSocketInfo : Serializable {
    var webSocket: WebSocket? = null
    var simpleMsg: String? = null
    var byteStringMsg: ByteString? = null
    var isConnected: Boolean = false
    var isReconnect: Boolean = false
    var isPreConnect: Boolean = false

    fun reset(): WebSocketInfo {
        this.webSocket = null
        this.simpleMsg = null
        this.byteStringMsg = null
        this.isConnected = false
        this.isReconnect = false
        return this
    }

    companion object {
        private const val serialVersionUID = 1714523747523892210L

        fun createReconnect(): WebSocketInfo {
            val socketInfo = WebSocketInfo()
            socketInfo.isReconnect = true
            return socketInfo
        }

        fun createConnected(webSocket: WebSocket): WebSocketInfo {
            val socketInfo = WebSocketInfo()
            socketInfo.isConnected = true
            socketInfo.webSocket = webSocket
            return socketInfo
        }

        fun createPreConnect(webSocket: WebSocket): WebSocketInfo {
            val socketInfo = WebSocketInfo()
            socketInfo.isPreConnect = true
            socketInfo.webSocket = webSocket
            return socketInfo
        }

        fun createMsg(webSocket: WebSocket, simpleMsg: String): WebSocketInfo {
            val socketInfo = WebSocketInfo()
            socketInfo.simpleMsg = simpleMsg
            socketInfo.webSocket = webSocket
            return socketInfo
        }

        fun createByteStringMsg(webSocket: WebSocket, byteMsg: ByteString): WebSocketInfo {
            val socketInfo = WebSocketInfo()
            socketInfo.byteStringMsg = byteMsg
            socketInfo.webSocket = webSocket
            return socketInfo
        }
    }
}
