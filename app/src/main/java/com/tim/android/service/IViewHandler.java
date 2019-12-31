package com.tim.android.service;

/**
 * IViewHandler
 *
 * @author Tell.Tim
 * @date 2019/12/30 16:42
 */
public interface IViewHandler {
    void onShowQrCode(String qrcode);
    void onShowTimeOut();
    void onShowNetError();
}
