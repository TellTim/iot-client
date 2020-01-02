package com.tim.iot;

import com.tim.iot.device.entity.AccountInfo;
import com.tim.iot.device.entity.QrCodeInfo;

/**
 * IIotClient
 *
 * @author Tell.Tim
 * @date 2019/12/27 14:59
 */
public interface IIotClient {
    String getAccount();

    void syncAuthorized(ISyncAuthorizedCallback callback);

    void syncQrCode(ISyncQrCodeCallback syncQrCodeCallback);

    interface ISyncAuthorizedCallback {
        void onSyncAuthorized(AccountInfo accountInfo);

        void onSyncUnAuthorized();

        void onSyncAuthorizedError(Exception e);
    }

    interface ISyncQrCodeCallback {
        /**
         * 已经授权成功
         *
         * @param accountInfo AccountInfo
         */
        void onSyncQrCodeAuthorized(AccountInfo accountInfo);

        /**
         * 同步到新的二维码
         *
         * @param qrCodeInfo QrCodeInfo
         */
        void onSyncQrCodeInfo(QrCodeInfo qrCodeInfo);

        /**
         * 同步二维码异常
         *
         * @param e Exception
         */
        void onSyncQrCodeError(Exception e);

        /**
         * 二维码过期
         */
        void onAuthTimeOut();
    }
}
