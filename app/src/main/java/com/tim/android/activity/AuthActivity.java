package com.tim.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.tim.common.AuthorizedEvent;
import com.tim.common.DeviceUtils;
import com.tim.common.Logger;
import com.tim.iot.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        logger.d("onCreate " + getTaskId());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logger.d("onNewIntent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.d("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.d("onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        logger.d("onDestroy");
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void when(AuthorizedEvent event) {
        finish();
    }
}
