package com.tim.android.service

/**
 * IViewHandler
 *
 * @author Tell.Tim
 * @date 2019/12/30 16:42
 */
interface IViewHandler {
    fun onShowQrCode(qrcode: String)
    fun onShowTimeOut()
    fun onShowNetError()
    fun onExit()
}
