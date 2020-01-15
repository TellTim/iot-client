package com.tim.android.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.tim.common.DeviceUtils;
import com.tim.iot.BuildConfig;
import com.tim.iot.R;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.trace.TraceClient;
import com.tim.iot.trace.domain.entity.TraceInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthActivity
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
public class TestActivity extends AppCompatActivity {

    TextView tvAppInfo;
    private boolean mShowRequestPermission = true;
    private static final int REQUEST_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvAppInfo = findViewById(R.id.tv_app);

        initPermission();
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE
            };
            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i])
                        != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }

            if (mPermissionList.isEmpty()) {
                mShowRequestPermission = true;
                tvAppInfo.setText("DeviceInfo: " + getDeviceInfo());
                TraceClient.getInstance().sendTrace(
                        new TraceInfo("vityTestActivityTestActivityTestActivityTestActivityTestActivity",
                                "initPermission", "test", "info"));
            } else {
                //存在未允许的权限
                String[] permissionsArr =
                        mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissionsArr, REQUEST_PERMISSION_CODE);
            }
        }
    }

    private String getDeviceInfo() {
        DeviceInfo deviceInfo =
                new DeviceInfo(DeviceUtils.getDeviceSerial(), DeviceUtils.getMacAddress(this),
                        DeviceUtils.getImei(this, BuildConfig.PRODUCT_TYPE),
                        BuildConfig.PRODUCT_TYPE);
        return String.format("%s?%s", BuildConfig.REGISTER_HOST,
                deviceInfo.toString().replaceAll(",", "&"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission =
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    permissions[i]);
                    if (showRequestPermission) {
                        initPermission();
                        return;
                    } else {
                        // false 被禁止了，不在访问
                        mShowRequestPermission = false;
                    }
                }
            }
        }
    }
}
