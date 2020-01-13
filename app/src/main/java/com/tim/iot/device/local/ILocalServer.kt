package com.tim.iot.device.local

/**
 * ILocalServer
 *
 * @author Tell.Tim
 * @date 2019/12/27 11:09
 */
interface ILocalServer {

    val account: String

    fun saveAuthToLocal(accountInfo: String)

    fun clearAuthorized()
}
