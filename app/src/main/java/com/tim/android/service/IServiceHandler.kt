package com.tim.android.service

/**
 * IServiceHandler
 *
 * @author Tell.Tim
 * @date 2019/12/30 16:31
 * 后台服务的句柄,用户bindService
 */
interface IServiceHandler {
    fun registerViewHandler(vewHandler: IViewHandler)
    fun unRegisterViewHandler(vewHandler: IViewHandler)
    fun retryHandler()
}
