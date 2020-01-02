package com.tim.android.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tim.android.activity.AuthActivity;
import com.tim.android.constant.AppAction;
import com.tim.android.constant.AppConst;
import com.tim.common.DeviceUtils;
import com.tim.common.INetConnectedCallback;
import com.tim.common.Logger;
import com.tim.iot.BuildConfig;
import com.tim.iot.IIotClient;
import com.tim.iot.IotClient;
import com.tim.iot.device.entity.AccountInfo;
import com.tim.iot.common.DeviceInfo;
import com.tim.iot.device.entity.QrCodeInfo;
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
public class CoreService extends Service
        implements SharedPreferences.OnSharedPreferenceChangeListener, INetConnectedCallback,
        IIotClient.ISyncQrCodeCallback, IIotClient.ISyncAuthorizedCallback,IServiceHandler {
    private static final Logger logger = Logger.getLogger("CoreService");

    private static final int THREAD_POOL_CORE_SIZE = 4;
    private static final int THREAD_POOL_MAX_SIZE = 20;
    private static final int THREAD_POOL_KEEP_ALIVE_TIME = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private IIotClient iotClient;
    private Context context;
    private SharedPreferences authSharedPref;
    private ConnectivityManager.NetworkCallback networkCallback;
    private BroadcastReceiver netConnectivityReceiver;
    private volatile IViewHandler viewHandler;
    private ExecutorService executorService;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        logger.d("onBind");
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.d("onCreate");
        this.context = this.getApplicationContext();
        initClient();
        initSharedPref();
    }

    private void initClient() {
        this.executorService =
                new ThreadPoolExecutor(THREAD_POOL_CORE_SIZE, THREAD_POOL_MAX_SIZE,
                        THREAD_POOL_KEEP_ALIVE_TIME,
                        TIME_UNIT, new LinkedBlockingDeque<>(), (ThreadFactory) Thread::new);
        DeviceInfo deviceInfo =
                new DeviceInfo(DeviceUtils.getDeviceSerial(), DeviceUtils.getMacAddress(context),
                        DeviceUtils.getImei(context, BuildConfig.PRODUCT_TYPE),
                        BuildConfig.PRODUCT_TYPE);
        this.iotClient = IotClient.getInstance(this, executorService, deviceInfo);
    }

    private void initSharedPref() {
        this.authSharedPref = getSharedPreferences(AppConst.AUTH_SHARED_PREF, MODE_PRIVATE);
        if ("".equals(authSharedPref.getString(AppConst.AUTH_ACCOUNT_ITEM, ""))) {
            authSharedPref.edit()
                    .putString(AppConst.AUTH_ACCOUNT_ITEM, AppConst.UN_AUTH_ACCOUNT_VALUE)
                    .apply();
        }
        authSharedPref.registerOnSharedPreferenceChangeListener(this);
        //本地授权标识为空，则跳转到授权界面.
        if (AppConst.UN_AUTH_ACCOUNT_VALUE.equals(
                authSharedPref.getString(AppConst.AUTH_ACCOUNT_ITEM,
                        AppConst.UN_AUTH_ACCOUNT_VALUE))) {
            logger.d("initSharedPref 触发跳转授权界面");
            startAuthView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.authSharedPref.unregisterOnSharedPreferenceChangeListener(this);
        unRegisterNetListener();
        logger.d("onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.d("onStartCommand");
        if (intent != null && AppAction.ACTION_BOOT_COMPLETE.equals(intent.getAction())) {
            registerNetLister(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerNetLister(INetConnectedCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (networkCallback == null) {
                networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        logger.d("onAvailable");
                        callback.onConnected();
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
                                            callback.onConnected();
                                        }
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

    private void unRegisterNetListener() {
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
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String item) {
        logger.d("onSharedPreferenceChanged " + item);
        if (AppConst.AUTH_ACCOUNT_ITEM.equals(item)) {
            // 表示状态由授权变更为未授权
            if (AppConst.UN_AUTH_ACCOUNT_VALUE.equals(
                    sharedPreferences.getString(AppConst.AUTH_ACCOUNT_ITEM,
                            AppConst.UN_AUTH_ACCOUNT_VALUE))) {
                logger.d("监听到未授权变更,触发跳转授权界面");
                startAuthView();
            }else{
                //表示状态由未授权变更为授权
                logger.d("监听到授权变更,触发结束授权界面");
                stopAuthView();
            }
        }
    }

    /**
     * 网络连接后,同步后端的设备授权状态
     */
    @Override
    public void onConnected() {
        syncRemoteAuth();
    }

    private void syncRemoteAuth() {
        logger.d("syncRemoteAuth: 开始同步服务端授权状态");
        iotClient.syncAuthorized(this);
    }

    @Override
    public void onSyncAuthorized(AccountInfo accountInfo) { }

    @Override
    public void onSyncUnAuthorized() {
        startAuthView();
    }

    @Override
    public void onSyncAuthorizedError(Exception e) { }

    /**
     * 同步QrCode,远端已经授权通过
     *
     * @param accountInfo AccountInfo
     */
    @Override
    public void onSyncQrCodeAuthorized(AccountInfo accountInfo) { }

    /**
     * 同步QrCode
     *
     * @param qrCodeInfo QrCodeInfo
     */
    @Override
    public void onSyncQrCodeInfo(QrCodeInfo qrCodeInfo) {
        if (viewHandler!=null){
            viewHandler.onShowQrCode(qrCodeInfo.getQrCode());
        }
    }

    /**
     * 同步QrCode，出现异常，此处需上报给异常处理器。等候分析
     * 有以下异常
     * 设备类型错误,
     * 与后端交互协议错误
     *
     * @param e Exception
     */
    @Override
    public void onSyncQrCodeError(Exception e) {
        if (viewHandler!=null){
            viewHandler.onShowNetError();
        }
    }

    @Override
    public void onAuthTimeOut() {
        if (viewHandler!=null){
            viewHandler.onShowTimeOut();
        }
    }

    /**
     * 启动授权界面,有且仅当
     * 开机检测本地sharedPref中的授权标记未为授权
     * 监听sharedPref的变更为未授权状态时触发
     */
    private void startAuthView() {
        logger.d("startAuthView");
        Intent activityIntent = new Intent(this, AuthActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }

    private void stopAuthView() {
        logger.d("stopAuthView");
        if (viewHandler!=null){
            viewHandler.onExit();
        }
    }

    /**
     * view bindService的方式,触发操作service中的syncQrCode
     * @param viewHandler IViewHandler
     */
    @Override
    public void registerViewHandler(IViewHandler viewHandler) {
        logger.d("registerViewHandler");
        this.viewHandler = viewHandler;
        this.iotClient.syncQrCode(this);
    }

    @Override
    public void unRegisterViewHandler(IViewHandler viewHandler) {
        logger.d("unRegisterViewHandler");
        if (this.viewHandler == viewHandler) {
            this.viewHandler = null;
        }
    }

    @Override
    public void retryHandler() {
        this.iotClient.syncQrCode(this);
    }

    public class Binder extends android.os.Binder {
        public IServiceHandler getServiceHandler() {
            return CoreService.this;
        }
    }
}
