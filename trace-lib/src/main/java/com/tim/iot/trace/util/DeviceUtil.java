package com.tim.iot.trace.util;

import android.os.Build;

/**
 * DeviceUtil
 *
 * @author Tell.Tim
 * @date 2020/1/15 13:59
 */
public class DeviceUtil {

    public static String getSystemVersion(){
        return Build.DISPLAY;
    }
}
