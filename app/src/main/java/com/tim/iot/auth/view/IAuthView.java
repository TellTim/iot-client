package com.tim.iot.auth.view;

/**
 * IAuthView
 *
 * @author Tell.Tim
 * @date 2019/12/25 9:57
 */
public interface IAuthView {
        void onShowAuthView(String qrCode);

        void onNetError();

        void onClose();
}
