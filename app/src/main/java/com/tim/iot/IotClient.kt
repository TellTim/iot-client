package com.tim.iot

import android.content.Context
import com.tim.common.DeviceUtils
import com.tim.common.ICallback
import com.tim.common.Logger
import com.tim.common.Respond
import com.tim.iot.auth.AuthServer
import com.tim.iot.auth.IAuthServer
import com.tim.iot.auth.entity.UrlInfo
import com.tim.iot.common.DeviceInfo
import com.tim.iot.device.entity.AccountInfo
import com.tim.iot.device.entity.QrCodeInfo
import com.tim.iot.device.local.ILocalServer
import com.tim.iot.device.local.LocalServer
import com.tim.iot.device.remote.DeviceService
import com.tim.iot.device.remote.IDeviceServer
import java.util.concurrent.ExecutorService

/**
 * IotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
class IotClient private constructor(private val context: Context,
    private val executorService: ExecutorService,
    private val deviceInfo: DeviceInfo) : IIotClient {
    private val authServer: IAuthServer
    private val registerServer: IDeviceServer
    private val localServer: ILocalServer

    override val account: String
        get() = localServer.account

    init {
        this.authServer = AuthServer()
        this.registerServer = DeviceService(executorService)
        this.localServer = LocalServer(context)
    }

    override fun syncAuthorized(callback: IIotClient.ISyncAuthorizedCallback) {
        this.registerServer.syncAuthorized(this.deviceInfo,
                object : ICallback<AccountInfo, Respond<*>> {
                    override fun onSuccess(accountInfo: AccountInfo) {
                        logger.d("syncAuthorized onSuccess accountInfo $accountInfo")
                        localServer.saveAuthToLocal(accountInfo.toString())
                        callback.onSyncAuthorized(accountInfo)
                    }

                    override fun onFail(respond: Respond<*>) {
                        if (Respond.State.BIND_NOT_EXIST == respond.state) {
                            logger.d("syncAuthorized onFail bind not exist")
                            localServer.clearAuthorized()
                            callback.onSyncUnAuthorized()
                        } else {
                            callback.onSyncAuthorizedError(Exception(respond.state!!.value))
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        logger.e("syncAuthorized onError " + throwable.message)
                        if (BuildConfig.DEBUG_REGISTER) {
                            localServer.clearAuthorized()
                            callback.onSyncUnAuthorized()
                        } else {
                            callback.onSyncAuthorizedError(Exception(throwable.message))
                        }
                    }
                })
    }

    @Synchronized override fun syncQrCode(callback: IIotClient.ISyncQrCodeCallback) {
        this.registerServer.syncQrCode(this.deviceInfo,
                object : ICallback<AccountInfo, Respond<*>> {
                    override fun onSuccess(accountInfo: AccountInfo) {
                        logger.d("syncQrCode Authorized accountInfo $accountInfo")
                        localServer.saveAuthToLocal(accountInfo.toString())
                        callback.onSyncQrCodeAuthorized(accountInfo)
                    }

                    override fun onFail(respond: Respond<*>) {
                        //只将未授权的回调出去，其他异常需修复处理
                        if (respond.state == Respond.State.BIND_NOT_EXIST) {
                            logger.d(
                                    "syncQrCode onSyncQrCodeInfo " + (respond.t as QrCodeInfo).qrCode!!)
                            val qrCodeInfo = respond.t as QrCodeInfo?
                            val urlInfo = UrlInfo(DeviceUtils.deviceSerial)
                            executorService.execute {
                                authServer.connect(urlInfo.toString(),
                                        qrCodeInfo!!.expireIn,
                                        object : IAuthServer.IConnectAuthServerCallback {
                                            override fun onConnectSuccess() {
                                                logger.d("onConnectSuccess")
                                                callback.onSyncQrCodeInfo(qrCodeInfo)
                                            }

                                            override fun onConfirm(accountInfo: AccountInfo) {
                                                logger.d("onConfirm")
                                                localServer.saveAuthToLocal(accountInfo.toString())
                                                callback.onSyncQrCodeAuthorized(accountInfo)
                                                authServer.closeConnect(urlInfo.toString())
                                            }

                                            override fun onTimeOut() {
                                                logger.d("onTimeOut")
                                                callback.onAuthTimeOut()
                                                authServer.forceClose()
                                            }

                                            override fun onConnectError(e: Exception) {
                                                logger.e("onConnectError " + e.message)
                                                callback.onSyncQrCodeError(e)
                                            }
                                        })
                            }
                        } else {
                            //todo 此处应该埋点,通过trace-lib动态上传给后端,等候分析异常.
                            callback.onSyncQrCodeError(Exception(respond.t as String?))
                            logger.e("syncQrCode onFail " + respond.t!!)
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        //todo 此处应该埋点,通过trace-lib动态上传给后端,等候分析异常.
                        logger.e("syncQrCode onError " + throwable.cause)
                        if (BuildConfig.DEBUG_REGISTER) {
                            val qrCodeInfo = QrCodeInfo()
                            qrCodeInfo.expireIn = 120
                            qrCodeInfo.qrCode = "adsfasdfasdfasdadfddd"
                            val urlInfo = UrlInfo(DeviceUtils.deviceSerial)
                            executorService.execute {
                                authServer.connect(urlInfo.toString(),
                                        qrCodeInfo.expireIn,
                                        object : IAuthServer.IConnectAuthServerCallback {
                                            override fun onConnectSuccess() {
                                                logger.d("onConnectSuccess")
                                                callback.onSyncQrCodeInfo(qrCodeInfo)
                                            }

                                            override fun onConfirm(accountInfo: AccountInfo) {
                                                logger.d("onConfirm")
                                                localServer.saveAuthToLocal(accountInfo.toString())
                                                callback.onSyncQrCodeAuthorized(accountInfo)
                                                authServer.closeConnect(urlInfo.toString())
                                            }

                                            override fun onTimeOut() {
                                                logger.d("onTimeOut")
                                                callback.onAuthTimeOut()
                                                authServer.forceClose()
                                            }

                                            override fun onConnectError(e: Exception) {
                                                logger.e("onConnectError " + e.message)
                                                callback.onSyncQrCodeError(e)
                                            }
                                        })
                            }
                        } else {
                            callback.onSyncQrCodeError(Exception(throwable.message))
                        }
                    }
                })
    }

    companion object {
        private val logger = Logger.getLogger("IotClient")
        private var instance: IIotClient? = null

        fun getInstance(context: Context, executorService: ExecutorService,
            deviceInfo: DeviceInfo): IIotClient {
            synchronized(AuthServer::class.java) {
                if (instance == null) {
                    instance = IotClient(context, executorService, deviceInfo)
                }
                return instance as IIotClient
            }
        }
    }
}
