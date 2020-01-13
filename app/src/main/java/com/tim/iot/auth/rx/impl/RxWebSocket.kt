package com.tim.iot.auth.rx.impl

import android.os.Looper
import android.os.SystemClock
import com.tim.common.Logger
import com.tim.iot.auth.heartbeat.HeartBeatTask
import com.tim.iot.auth.entity.WebSocketCloseEnum
import com.tim.iot.auth.entity.WebSocketInfo
import com.tim.iot.auth.exception.OverReconnectCountException
import com.tim.iot.auth.heartbeat.HeartBeatTask.IHeartBeatCallback
import com.tim.iot.auth.rx.IWebSocket
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.ProtocolException
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.decodeBase64

/**
 * @author Tell.Tim
 * @date 2019/12/2 15:40
 */
class RxWebSocket(client: OkHttpClient, private val mReconnectInterval: Long,
    private val mReconnectIntervalTimeUnit: TimeUnit, private val showLog: Boolean, logTag: String,
    sslSocketFactory: SSLSocketFactory?, trustManager: X509TrustManager?, enableHeartBeat: Boolean,
    heartBeatHead: String) : IWebSocket {
    private var mClient: OkHttpClient? = null
    private val observableWebSocketInfoMap: MutableMap<String, Observable<WebSocketInfo>>
    private val webSocketMap: MutableMap<String, WebSocket>
    private val mSslSocketFactory: SSLSocketFactory? = null
    private val mTrustManager: X509TrustManager? = null
    private val logger: Logger = Logger.getLogger("$logTag-client")
    private var currentReconnectCount: Int = 0
    private val heartBeatTask: HeartBeatTask

    init {
        this.heartBeatTask = HeartBeatTask(enableHeartBeat, HEART_BEAT_INTERVAL,
                heartBeatHead.decodeBase64()!!)
        if (sslSocketFactory != null && trustManager != null) {
            this.mClient =
                client.newBuilder().sslSocketFactory(sslSocketFactory, trustManager).build()
        } else {
            this.mClient = client
        }
        this.observableWebSocketInfoMap = ConcurrentHashMap()
        this.webSocketMap = ConcurrentHashMap()
        this.currentReconnectCount = 0
    }

    /**
     * 当遇到网络波荡异常时，需要自动重新连接
     *
     * @param url String
     * @param timeoutOfSecond int
     * @return Observable<WebSocketInfo>
    </WebSocketInfo> */
    override fun connect(url: String, timeoutOfSecond: Int): Observable<WebSocketInfo> {
        var observable = observableWebSocketInfoMap[url]
        if (observable == null) {
            observable = Observable.create(WebSocketOnSubscribe(url))
                    //超时设置
                    .timeout(timeoutOfSecond.toLong(), TimeUnit.SECONDS)
                    //重连
                    .retry { throwable ->
                        if (showLog) {
                            if (throwable is TimeoutException) {
                                logger.dFormat("%d %s 超时未反馈,断开连接",
                                        timeoutOfSecond, TimeUnit.SECONDS.toString())
                                return@Observable.create(new WebSocketOnSubscribe url)
                                        //超时设置
                                        .timeout(timeoutOfSecond, TimeUnit.SECONDS)
                                        //重连
                                        .retry false
                            } else if (throwable is ProtocolException) {
                                logger.eFormat("网络交互异常: %s",
                                        throwable.toString())
                                return@Observable.create(new WebSocketOnSubscribe url)
                                        //超时设置
                                        .timeout(timeoutOfSecond, TimeUnit.SECONDS)
                                        //重连
                                        .retry false
                            } else if (throwable is OverReconnectCountException) {
                                logger.eFormat("重连次数已达上限制: %s",
                                        throwable.toString())
                                return@Observable.create(new WebSocketOnSubscribe url)
                                        //超时设置
                                        .timeout(timeoutOfSecond, TimeUnit.SECONDS)
                                        //重连
                                        .retry false
                            } else if (throwable is IOException) {
                                logger.eFormat("网络出现异常，%d %s 后重连",
                                        mReconnectInterval,
                                        mReconnectIntervalTimeUnit.toString())
                            }
                        }
                        throwable is IOException
                    }
                    .doOnDispose {
                        if (showLog) {
                            logger.d("订阅取消,断开连接")
                        }
                        closeNow(url)
                    }
                    .share()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

            observableWebSocketInfoMap[url] = observable
            if (showLog) {
                logger.d("插入缓存成功 ")
            }
        } else {
            if (showLog) {
                logger.d("从缓存连接池中取出")
            }
            val webSocket = webSocketMap[url]
            if (webSocket != null) {
                observable = observable.startWith(WebSocketInfo.createPreConnect(webSocket))
            } else {
                if (showLog) {
                    logger.e("从缓存获取为空")
                }
            }
        }
        return observable!!.observeOn(AndroidSchedulers.mainThread())
    }

    private fun closeNow(url: String) {
        closeWebSocket(webSocketMap[url], false)
    }

    private fun closeWebSocket(webSocket: WebSocket?, force: Boolean) {
        if (webSocket == null) {
            if (showLog) {
                logger.dFormat(" --> 连接已不存在,缓冲池中还剩下%d个监听", observableWebSocketInfoMap.size)
            }
            return
        }
        if (showLog) {
            logger.d("closeWebSocket")
        }
        val closeEnum: WebSocketCloseEnum
        if (force) {
            closeEnum = WebSocketCloseEnum.FORCE_EXIT
        } else {
            closeEnum = WebSocketCloseEnum.USER_EXIT
        }
        val result = webSocket.close(closeEnum.code, closeEnum.reason)
        if (result) {
            removeUrlWebSocketMapping(webSocket)
            if (showLog) {
                logger.dFormat("关闭连接成功,缓存池中还剩下%d个监听,%d个连接实例", webSocketMap.size,
                        observableWebSocketInfoMap.size)
            }
        } else {
            if (force) {
                removeUrlWebSocketMapping(webSocket)
            }
            if (showLog) {
                logger.eFormat("连接已处理关闭,缓存池中还剩下%d个监听,%d个连接实例", webSocketMap.size,
                        observableWebSocketInfoMap.size)
            }
        }
    }

    private fun removeUrlWebSocketMapping(webSocket: WebSocket) {
        if (showLog) {
            logger.d("removeUrlWebSocketMapping")
        }
        for ((url, value) in webSocketMap) {
            if (value === webSocket) {
                observableWebSocketInfoMap.remove(url)
                webSocketMap.remove(url)
            }
        }
    }

    override fun closeConnect(url: String) {
        if (showLog) {
            logger.d("关闭连接")
        }
        val observable = observableWebSocketInfoMap[url]
        if (observable != null) {
            val disposable = observable.subscribe()
            if (!disposable.isDisposed) {
                disposable.dispose()
            } else {
                if (showLog) {
                    logger.d("disposable.isDisposed")
                }
            }
        } else {
            if (showLog) {
                logger.d("observable == null")
            }
        }
    }

    private inner class WebSocketOnSubscribe internal constructor(
        private val mWebSocketUrl: String) : ObservableOnSubscribe<WebSocketInfo> {
        private var mWebSocket: WebSocket? = null

        @Throws(Exception::class)
        override fun subscribe(emitter: ObservableEmitter<WebSocketInfo>) {
            if (showLog) {
                logger.d("开始订阅")
            }
            if (mWebSocket != null) {
                if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                    if (currentReconnectCount >= MAX_RECONNECT_COUNT) {
                        emitter.onError(OverReconnectCountException("reconnect over size"))
                        currentReconnectCount = 0
                        return
                    } else {
                        currentReconnectCount++
                    }
                    var millis = mReconnectIntervalTimeUnit.toMillis(mReconnectInterval)
                    if (millis == 0L) {
                        millis = DEFAULT_TIMEOUT * 1500
                    }
                    if (showLog) {
                        logger.dFormat(" --> %d秒后即将重连[%d]", millis / 1000, currentReconnectCount)
                    }
                    SystemClock.sleep(millis)
                    emitter.onNext(WebSocketInfo.createReconnect())
                }
            }
            initWebSocket(emitter)
        }

        @Synchronized private fun initWebSocket(emitter: ObservableEmitter<WebSocketInfo>) {
            mWebSocket =
                mClient!!.newWebSocket(getRequest(mWebSocketUrl), object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocketMap[mWebSocketUrl] = webSocket
                        currentReconnectCount = 0
                        if (showLog) {
                            logger.d("onOpen,连接已建立,新连接插入连接池中")
                        }

                        if (!emitter.isDisposed) {
                            if (showLog) {
                                logger.d("上报已经连接状态")
                            }
                            emitter.onNext(WebSocketInfo.createConnected(webSocket))
                        }
                        //开启发送心跳
                        heartBeatTask.start(IHeartBeatCallback { webSocket.send(it) })
                        heartBeatTask.start(()->{webSocket.send()})
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        if (showLog) {
                            logger.d("onMessage,收到新消息:$text")
                        }
                        if (!emitter.isDisposed) {
                            if (showLog) {
                                logger.d("上报收到新消息")
                            }
                            emitter.onNext(WebSocketInfo.createMsg(webSocket, text))
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        if (showLog) {
                            logger.d("onMessage,收到新消息")
                        }
                        if (!emitter.isDisposed) {
                            if (showLog) {
                                logger.d("onMessage,上报收到新消息")
                            }
                            emitter.onNext(
                                    WebSocketInfo.createByteStringMsg(webSocket, bytes))
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable,
                        response: Response?) {
                        if (showLog) {
                            logger.eFormat("%s onFailure %s %s", mWebSocketUrl,
                                    t.toString(),
                                    webSocket.request().url.toUri().path)
                        }
                        if (!emitter.isDisposed) {
                            if (showLog) {
                                logger.eFormat("%s上报异常: ", mWebSocketUrl,
                                        t.toString())
                            }
                            emitter.onError(t)
                        }
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        if (showLog) {
                            logger.dFormat(
                                    "%s onClosing:code=%d,reason=%s", mWebSocketUrl,
                                    code, reason)
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        if (showLog) {
                            logger.dFormat(
                                    "%s onClosed:code=%d,reason=%s", mWebSocketUrl,
                                    code, reason)
                        }
                    }
                })
            emitter.setCancellable {
                if (showLog) {
                    logger.d("$mWebSocketUrl 取消连接")
                }
                //停止心跳发送
                heartBeatTask.stop()
                mWebSocket!!.close(3000, "close WebSocket")

            }
        }
    }

    private fun getRequest(url: String): Request {
        return Request.Builder().get().url(url).build()
    }

    override fun forceClose() {
        for ((_, value) in webSocketMap) {
            closeWebSocket(value, true)
        }
    }

    companion object {
        private val DEFAULT_TIMEOUT = 5L
        private val HEART_BEAT_INTERVAL = 2
        private val MAX_RECONNECT_COUNT = 30
    }
}
