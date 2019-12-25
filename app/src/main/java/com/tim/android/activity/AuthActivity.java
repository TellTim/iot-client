package com.tim.android.activity;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.tim.common.DeviceUtils;
import com.tim.iot.BuildConfig;
import com.tim.iot.R;

/**
 * AuthActivity
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
public class AuthActivity extends AppCompatActivity {

    TextView tvInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        tvInfo = findViewById(R.id.tv_info);
        tvInfo.setText(String.format("Device:%s", DeviceUtils.getDeviceSerial()));
    }
}
