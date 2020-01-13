package com.tim.iot.auth

import android.text.TextUtils
import com.tim.common.Logger
import com.tim.iot.auth.entity.WebSocketInfo
import com.tim.iot.auth.rx.IWebSocket
import com.tim.iot.device.entity.AccountInfo
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeoutException

/**
 * AuthServer
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:33
 */
class AuthServer : IAuthServer {
    private val webSocket: IWebSocket
    private var disposable: Disposable? = null

    init {
        this.webSocket = Builder().setHeartBeat(false).setShowLog(true, TAG).build()
    }

    @Synchronized override fun connect(url: String, timeoutOfSecond: Int,
        connectAuthServerCallback: IAuthServer.IConnectAuthServerCallback) {
        this.webSocket.connect(url, timeoutOfSecond).subscribe(object : Observer<WebSocketInfo> {
            override fun onSubscribe(d: Disposable) {
                logger.d("订阅关系已建立")
                disposable = d
            }

            override fun onNext(webSocketInfo: WebSocketInfo) {
                if (webSocketInfo.isConnected) {
                    logger.d("连接成功")
                    connectAuthServerCallback.onConnectSuccess()
                } else if (webSocketInfo.simpleMsg != null) {
                    val simpleMessage = webSocketInfo.simpleMsg
                    logger.d("收到消息: " + simpleMessage!!)
                    if (!TextUtils.isEmpty(simpleMessage) && simpleMessage.startsWith("confirm")) {
                        val convertArray =
                            simpleMessage.split("#".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                        val accountInfo = AccountInfo()
                        accountInfo.account = convertArray[1]
                        accountInfo.createAt = java.lang.Long.valueOf(convertArray[2])
                        connectAuthServerCallback.onConfirm(accountInfo)
                    }
                } else if (webSocketInfo.byteStringMsg != null) {
                    logger.d("receive byte message")
                } else if (webSocketInfo.isReconnect) {
                    logger.d("正在重连")
                } else if (webSocketInfo.isPreConnect) {
                    logger.d("准备连接")
                }
            }

            override fun onError(e: Throwable) {
                if (e is TimeoutException) {
                    logger.d("超时未响应")
                    connectAuthServerCallback.onTimeOut()
                } else {
                    logger.d("连接异常 " + e.message)
                    connectAuthServerCallback.onConnectError(Exception(e))
                }
            }

            override fun onComplete() {
                logger.d(" --> 处理完毕")
            }
        })
    }

    override fun closeConnect(url: String) {
        logger.d("closeConnect")
        if (!disposable!!.isDisposed) {
            logger.d("dispose")
            disposable!!.dispose()
        } else {
            logger.d("dispose isDisposed")
        }
    }

    override fun forceClose() {
        this.webSocket.forceClose()
    }

    companion object {
        private val TAG = "AuthServer"
        private val logger = Logger.getLogger(TAG)
    }
}