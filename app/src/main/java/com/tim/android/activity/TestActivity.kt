package com.tim.android.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tim.common.DeviceUtils
import com.tim.iot.BuildConfig
import com.tim.iot.R
import com.tim.iot.common.DeviceInfo
import java.util.ArrayList

/**
 * AuthActivity
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
class TestActivity : AppCompatActivity() {

    internal var tvAppInfo: TextView
    private var mShowRequestPermission = true

    private val deviceInfo: String
        get() {
            val deviceInfo = DeviceInfo(DeviceUtils.deviceSerial, DeviceUtils.getMacAddress(this),
                    DeviceUtils.getImei(this, BuildConfig.PRODUCT_TYPE), BuildConfig.PRODUCT_TYPE)
            return String.format("%s?%s", BuildConfig.REGISTER_HOST,
                    deviceInfo.toString().replace(",".toRegex(), "&"))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvAppInfo = findViewById(R.id.tv_app)

        initPermission()
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE)
            val mPermissionList = ArrayList<String>()
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(this,
                                permissions[i]) != PackageManager.PERMISSION_GRANTED
                ) {
                    mPermissionList.add(permissions[i])
                }
            }

            if (mPermissionList.isEmpty()) {
                mShowRequestPermission = true
                tvAppInfo.text = "DeviceInfo: $deviceInfo"
            } else {
                //存在未允许的权限
                val permissionsArr = mPermissionList.toTypedArray()
                ActivityCompat.requestPermissions(this, permissionsArr, REQUEST_PERMISSION_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    val showRequestPermission =
                        ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])
                    if (showRequestPermission) {
                        initPermission()
                        return
                    } else {
                        // false 被禁止了，不在访问
                        mShowRequestPermission = false
                    }
                }
            }
        }
    }

    companion object {
        private val REQUEST_PERMISSION_CODE = 101
    }
}
