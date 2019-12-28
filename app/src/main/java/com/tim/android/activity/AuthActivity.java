package com.tim.android.activity;

import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.tim.common.DeviceUtils;
import com.tim.common.Logger;
import com.tim.iot.BuildConfig;
import com.tim.iot.R;

/**
 * AuthActivity
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
public class AuthActivity extends AppCompatActivity {
  private static final String TAG = "AuthActivity";
  private static final Logger logger = Logger.getLogger(TAG);
    TextView tvInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        tvInfo = findViewById(R.id.tv_info);
        tvInfo.setText(String.format("Device:%s", DeviceUtils.getDeviceSerial()));
        logger.d("onCreate "+getTaskId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logger.d("onNewIntent");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
