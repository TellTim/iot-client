package com.tim.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import com.tim.iot.BuildConfig
import java.io.FileInputStream
import java.net.NetworkInterface
import java.util.Enumeration
import java.util.Objects

/**
 * DeviceUtils
 *
 * @author Tell.Tim
 * @date 2019/12/25 11:12
 */
object DeviceUtils {
    private val TAG = "DeviceUtils"
    private val PREFERENCE_NAME = "device"
    private val KEY_MAC_ADDRESS = "mac_address"
    private val ADDRESS_MAC_DEFAULT = "02:00:00:00:00:00"
    private val logger = Logger.getLogger(TAG)

    /**
     * 获取设备系统版本号
     *
     * @return 设备系统版本号
     */
    val sdkVersionName: String
        get() = Build.VERSION.RELEASE

    /**
     * 获取设备系统版本码
     *
     * @return 设备系统版本码
     */
    val sdkVersionCode: Int
        get() = Build.VERSION.SDK_INT

    val apkVersionName: String
        get() = BuildConfig.VERSION_NAME

    val apkVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    val deviceSerial: String
        get() {
            var deviceId = ""
            if ("AWT-WIFI-C3" == BuildConfig.PRODUCT_TYPE) {
                deviceId = getDeviceProperty("snum")
            } else {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                        deviceId = Build.SERIAL
                    } else {
                        deviceId = Build.getSerial()
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
            return deviceId
        }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getMacAddress(context: Context): String? {
        var macAddress = getMacFromPref(context)
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = getMacAddressByNetworkInterface(false)
            if (!TextUtils.isEmpty(macAddress)) {
                saveMacToPref(context, macAddress)
            }
        }
        logger.d("getMacAddress: macAddress is " + macAddress!!)
        return macAddress
    }

    private fun getMacFromPref(context: Context): String? {
        val sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        var localMac = sp.getString(KEY_MAC_ADDRESS, ADDRESS_MAC_DEFAULT)
        if (localMac == ADDRESS_MAC_DEFAULT) {
            localMac = null
        }
        logger.d("getMacFromPref ")
        return localMac
    }

    private fun getMacAddressByNetworkInterface(splitSymbol: Boolean?): String {
        try {
            val nis = NetworkInterface.getNetworkInterfaces()
            while (nis.hasMoreElements()) {
                val ni = nis.nextElement()
                if (ni == null || !"wlan0".equals(ni.name, ignoreCase = true)) {
                    continue
                }
                val macBytes = ni.hardwareAddress
                if (macBytes != null && macBytes.size > 0) {
                    val sb = StringBuilder()
                    for (b in macBytes) {
                        if (splitSymbol!!) {
                            sb.append(String.format("%02X:", b))
                        } else {
                            sb.append(String.format("%02X", b))
                        }
                    }
                    return sb.substring(0, sb.length)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun saveMacToPref(context: Context, mac: String) {
        val sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(KEY_MAC_ADDRESS, mac)
        editor.apply()
    }

    @SuppressLint("MissingPermission")
    fun getImei(context: Context, produceType: String): String {
        val imei: String
        if ("AWT-WIFI-C3" == produceType) {
            imei = getDeviceProperty("imei")
        } else {
            imei = getImeiProperty(context)
        }
        return imei
    }

    @SuppressLint("HardwareIds")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    fun getImeiProperty(context: Context): String {
        var imei: String
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imei = tm.imei
            if (TextUtils.isEmpty(imei)) {
                imei = tm.meid
            }
        } else {
            try {
                imei = tm.deviceId
            } catch (e: Exception) {
                imei = ""
            }
        }
        logger.d("getImei is $imei")
        return imei
    }

    private fun getDeviceProperty(property: String): String {
        var result = property
        try {
            val fileName = "/data/privdata/ULI/factory/\$property.txt"
            val mFIS = FileInputStream(fileName)
            val mBuf = ByteArray(64)
            val rLen = mFIS.read(mBuf)
            result = String(mBuf, 0, rLen)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}
