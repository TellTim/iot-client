package com.tim.android.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tim.android.activity.AuthActivity;
import com.tim.common.DeviceUtils;
import com.tim.common.Logger;
import com.tim.iot.BuildConfig;
import com.tim.iot.auth.IIotClient;
import com.tim.iot.auth.IotClient;
import com.tim.iot.auth.view.IAuthView;
import com.tim.iot.common.DeviceInfo;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CoreService
 *
 * @author Tell.Tim
 * @date 2019/12/24 9:52
 */
public class CoreService extends Service implements IAuthView {
    private static final Logger logger = Logger.getLogger("CoreService");

    private static final int THREAD_POOL_CORE_SIZE = 4;
    private static final int THREAD_POOL_MAX_SIZE = 20;
    private static final int THREAD_POOL_KEEP_ALIVE_TIME = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private ConnectivityManager.NetworkCallback networkCallback;
    private BroadcastReceiver netConnectivityReceiver;
    private IIotClient iotClient;
    private Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.d("onCreate");
        context = this.getApplicationContext();
        ExecutorService executorService =
                new ThreadPoolExecutor(THREAD_POOL_CORE_SIZE, THREAD_POOL_MAX_SIZE,
                        THREAD_POOL_KEEP_ALIVE_TIME,
                        TIME_UNIT, new LinkedBlockingDeque<>(), (ThreadFactory) Thread::new);
        DeviceInfo deviceInfo =
                new DeviceInfo(DeviceUtils.getDeviceSerial(), DeviceUtils.getMacAddress(context),
                        DeviceUtils.getImei(context, BuildConfig.PRODUCT_TYPE), BuildConfig.PRODUCT_TYPE);
        iotClient = IotClient.getInstance(this, executorService, this,deviceInfo);
        context = this.getApplicationContext();
        registerNetLister();
    }

    private void registerNetLister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (networkCallback == null) {
                networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        logger.d("onAvailable");
                        iotClient.work();
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        logger.d("onLost");
                    }

                    @Override
                    public void onUnavailable() {
                        logger.d("onUnavailable");
                    }

                    @Override
                    public void onCapabilitiesChanged(@NonNull Network network,
                            @NonNull NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                        logger.d("onCapabilitiesChanged:" + networkCapabilities.toString());
                    }
                };
            }
            Objects.requireNonNull(getSystemService(ConnectivityManager.class))
                    .registerDefaultNetworkCallback(networkCallback);
        } else {
            if (netConnectivityReceiver == null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                netConnectivityReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        logger.d("onReceive network change " + intent.getAction());
                        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
                            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(
                                    intent.getAction())) {
                                //获取联网状态的NetworkInfo对象
                                NetworkInfo info = intent.getParcelableExtra(
                                        ConnectivityManager.EXTRA_NETWORK_INFO);
                                if (info != null) {
                                    //如果当前的网络连接成功并且网络连接可用
                                    if (NetworkInfo.State.CONNECTED == info.getState()
                                            && info.isAvailable()) {
                                        if (info.getType() == ConnectivityManager.TYPE_WIFI
                                                || info.getType()
                                                == ConnectivityManager.TYPE_MOBILE) {
                                            logger.d(info.getTypeName() + " 连上");
                                        }
                                        iotClient.work();
                                    } else {
                                        logger.d(info.getTypeName() + " 断开");
                                    }
                                }
                            }
                        }
                    }
                };
                registerReceiver(netConnectivityReceiver, filter);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.d("onDestroy");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (networkCallback != null) {
                Objects.requireNonNull(getSystemService(ConnectivityManager.class))
                        .unregisterNetworkCallback(networkCallback);
            }
        } else {
            if (netConnectivityReceiver != null) {
                unregisterReceiver(netConnectivityReceiver);
                netConnectivityReceiver = null;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override public void onShowAuthView(String qrCode) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setAction("");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

    @Override public void onNetError() {

    }

    @Override public void onClose() {

    }
}
