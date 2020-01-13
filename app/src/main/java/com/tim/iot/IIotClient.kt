package com.tim.iot

import com.tim.iot.device.entity.AccountInfo
import com.tim.iot.device.entity.QrCodeInfo

/**
 * IIotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
interface IIotClient {
    val account: String

    fun syncAuthorized(callback: ISyncAuthorizedCallback)

    fun syncQrCode(syncQrCodeCallback: ISyncQrCodeCallback)

    interface ISyncAuthorizedCallback {
        fun onSyncAuthorized(accountInfo: AccountInfo)

        fun onSyncUnAuthorized()

        fun onSyncAuthorizedError(e: Exception)
    }

    interface ISyncQrCodeCallback {
        /**
         * 已经授权成功
         *
         * @param accountInfo AccountInfo
         */
        fun onSyncQrCodeAuthorized(accountInfo: AccountInfo)

        /**
         * 同步到新的二维码
         *
         * @param qrCodeInfo QrCodeInfo
         */
        fun onSyncQrCodeInfo(qrCodeInfo: QrCodeInfo)

        /**
         * 同步二维码异常
         *
         * @param e Exception
         */
        fun onSyncQrCodeError(e: Exception)

        /**
         * 二维码过期
         */
        fun onAuthTimeOut()
    }
}
