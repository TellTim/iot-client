package com.tim.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import androidx.annotation.RequiresPermission;
import com.tim.iot.BuildConfig;
import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Objects;

/**
 * DeviceUtils
 *
 * @author Tell.Tim
 * @date 2019/12/25 11:12
 */
public class DeviceUtils {
    private static final String TAG = "DeviceUtils";
    private static final String PREFERENCE_NAME = "device";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String ADDRESS_MAC_DEFAULT = "02:00:00:00:00:00";
    private static final Logger logger = Logger.getLogger(TAG);

    /**
     * 获取设备系统版本号
     *
     * @return 设备系统版本号
     */
    public static String getSDKVersionName() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取设备系统版本码
     *
     * @return 设备系统版本码
     */
    public static int getSDKVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    public static String getApkVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static int getApkVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceSerial() {
        String deviceId = "";
        if ("AWT-WIFI-C3".equals(BuildConfig.PRODUCT_TYPE)) {
            deviceId = getDeviceProperty("snum");
        } else {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                    deviceId = Build.SERIAL;
                } else {
                    deviceId = Build.getSerial();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return deviceId;
    }

    public static String getMacAddress(Context context) {
        String macAddress = getMacFromPref(context);
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = getMacAddressByNetworkInterface(false);
            if (!TextUtils.isEmpty(macAddress)) {
                saveMacToPref(context, macAddress);
            }
        }
        logger.d("getMacAddress: macAddress is " + macAddress);
        return macAddress;
    }

    private static String getMacFromPref(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String localMac = sp.getString(KEY_MAC_ADDRESS, ADDRESS_MAC_DEFAULT);
        if (Objects.equals(localMac, ADDRESS_MAC_DEFAULT)) {
            localMac = null;
        }
        logger.d("getMacFromPref ");
        return localMac;
    }

    private static String getMacAddressByNetworkInterface(Boolean splitSymbol) {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni == null || !"wlan0".equalsIgnoreCase(ni.getName())) {
                    continue;
                }
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : macBytes) {
                        if (splitSymbol) {
                            sb.append(String.format("%02X:", b));
                        } else {
                            sb.append(String.format("%02X", b));
                        }
                    }
                    return sb.substring(0, sb.length());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void saveMacToPref(Context context, String mac) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_MAC_ADDRESS, mac);
        editor.apply();
    }

    @SuppressLint("MissingPermission")
    public static String getImei(Context context,String produceType) {
        String imei;
        if ("AWT-WIFI-C3".equals(produceType)) {
            imei = getDeviceProperty("imei");
        } else {
            imei = getImeiProperty(context);
        }
        return imei;
    }

    @SuppressLint("HardwareIds")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    public static String getImeiProperty(Context context) {
        String imei;
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imei = tm.getImei();
            if (TextUtils.isEmpty(imei)) {
                imei = tm.getMeid();
            }
        } else {
            try {
                imei = tm.getDeviceId();
            } catch (Exception e) {
                imei = "";
            }
        }
        logger.d("getImei is " + imei);
        return imei;
    }

    private static String getDeviceProperty(String property) {
        String result = property;
        try {
            String fileName = "/data/privdata/ULI/factory/$property.txt";
            FileInputStream mFIS = new FileInputStream(fileName);
            byte[] mBuf = new byte[64];
            int rLen = mFIS.read(mBuf);
            result = new String(mBuf, 0, rLen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
