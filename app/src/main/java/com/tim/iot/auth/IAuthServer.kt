package com.tim.iot.auth

import com.tim.iot.device.entity.AccountInfo

/**
 * IAuthServer
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
interface IAuthServer {
    fun connect(param: String, timeoutOfSecond: Int,
        connectAuthServerCallback: IConnectAuthServerCallback)

    fun closeConnect(url: String)

    fun forceClose()

    interface IConnectAuthServerCallback {
        fun onConnectSuccess()
        fun onConnectError(e: Exception)
        fun onConfirm(accountInfo: AccountInfo)
        fun onTimeOut()
    }
}
