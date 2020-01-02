package com.tim.android.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import com.tim.android.service.CoreService;
import com.tim.android.service.IServiceHandler;
import com.tim.android.service.IViewHandler;
import com.tim.common.Logger;
import com.tim.android.utils.UIUtils;
import com.tim.iot.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * AuthActivity
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
public class AuthActivity extends AppCompatActivity implements ServiceConnection, IViewHandler {
    private static final String TAG = "AuthActivity";
    private static final Logger logger = Logger.getLogger(TAG);
    private IServiceHandler serviceHandler;
    ImageView imgQrCode;
    ProgressBar pbQrCode;
    TextView tvErrorMsg;
    LinearLayout lLayoutErrorMsg;
    private Disposable subscribe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        imgQrCode = findViewById(R.id.img_qrcode);
        pbQrCode = findViewById(R.id.qr_code_progress);
        tvErrorMsg = findViewById(R.id.tv_error);
        lLayoutErrorMsg = findViewById(R.id.error_field);
        logger.d("onCreate " + getTaskId());
        bindService(new Intent(this, CoreService.class), this, Service.BIND_WAIVE_PRIORITY);
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
        if (this.subscribe!=null&&!this.subscribe.isDisposed()){
            this.subscribe.dispose();
        }
        if (serviceHandler != null) {
            serviceHandler.unRegisterViewHandler(this);
        }
        unbindService(this);
        logger.d("onDestroy");
    }

    @Override
    public void onBackPressed() {
        //屏蔽返回键
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        serviceHandler = ((CoreService.Binder) binder).getServiceHandler();
        serviceHandler.registerViewHandler(this);
        tvErrorMsg.setOnClickListener(view -> {
            //todo
            serviceHandler.retryHandler();
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onShowQrCode(String qrCode) {
        if (!TextUtils.isEmpty(qrCode)) {
            subscribe = Observable.just(
                    QRCodeEncoder.syncEncodeQRCode(qrCode, UIUtils.dip2Px(this, 200)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        imgQrCode.setVisibility(View.VISIBLE);
                        imgQrCode.setImageBitmap(bitmap);
                        pbQrCode.setVisibility(View.GONE);
                        lLayoutErrorMsg.setVisibility(View.GONE);
                    }, throwable -> logger.e("error " + throwable.getLocalizedMessage()));
        }
    }

    @Override
    public void onShowTimeOut() {
        runOnUiThread(()->{
            imgQrCode.setVisibility(View.GONE);
            pbQrCode.setVisibility(View.GONE);
            lLayoutErrorMsg.setVisibility(View.VISIBLE);
            tvErrorMsg.setText("授权超时,请重新获取二维码");
        });
    }

    @Override
    public void onShowNetError() {
        runOnUiThread(()->{
            imgQrCode.setVisibility(View.GONE);
            pbQrCode.setVisibility(View.GONE);
            lLayoutErrorMsg.setVisibility(View.VISIBLE);
            tvErrorMsg.setText("网络异常,请稍后重试");
        });
    }

    @Override
    public void onExit() {
        logger.d("授权通过,退出界面");
        finish();
    }
}
