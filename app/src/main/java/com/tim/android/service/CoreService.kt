package com.tim.android.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.IBinder
import com.tim.android.activity.AuthActivity
import com.tim.android.constant.AppAction
import com.tim.android.constant.AppConst
import com.tim.common.DeviceUtils
import com.tim.common.INetConnectedCallback
import com.tim.common.Logger
import com.tim.iot.BuildConfig
import com.tim.iot.IIotClient
import com.tim.iot.IotClient
import com.tim.iot.device.entity.AccountInfo
import com.tim.iot.common.DeviceInfo
import com.tim.iot.device.entity.QrCodeInfo
import java.util.Objects
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * CoreService
 *
 * @author Tell.Tim
 * @date 2019/12/24 9:52
 */
class CoreService : Service(), SharedPreferences.OnSharedPreferenceChangeListener,
        INetConnectedCallback, IIotClient.ISyncQrCodeCallback, IIotClient.ISyncAuthorizedCallback,
        IServiceHandler {
    private var iotClient: IIotClient? = null
    private var context: Context? = null
    private var authSharedPref: SharedPreferences? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var netConnectivityReceiver: BroadcastReceiver? = null
    @Volatile private var viewHandler: IViewHandler? = null
    private var executorService: ExecutorService? = null
    override fun onBind(intent: Intent): IBinder? {
        logger.d("onBind")
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        logger.d("onCreate")
        this.context = this.applicationContext
        initClient()
        initSharedPref()
    }

    private fun initClient() {
        this.executorService = ThreadPoolExecutor(THREAD_POOL_CORE_SIZE, THREAD_POOL_MAX_SIZE,
                THREAD_POOL_KEEP_ALIVE_TIME.toLong(),
                TIME_UNIT, LinkedBlockingDeque(), ThreadFactory { Thread(it) })
        val deviceInfo =
            context?.let { it ->
                DeviceUtils.getMacAddress(it)?.let {
                    DeviceInfo(DeviceUtils.deviceSerial, it,
                            DeviceUtils.getImei(context!!, BuildConfig.PRODUCT_TYPE),
                            BuildConfig.PRODUCT_TYPE)
                }
            }
        this.iotClient =
            deviceInfo?.let {
                IotClient.getInstance(this, executorService as ThreadPoolExecutor, it)
            }
    }

    private fun initSharedPref() {
        this.authSharedPref = getSharedPreferences(AppConst.AUTH_SHARED_PREF, Context.MODE_PRIVATE)
        if ("" == authSharedPref!!.getString(AppConst.AUTH_ACCOUNT_ITEM, "")) {
            authSharedPref!!.edit()
                    .putString(AppConst.AUTH_ACCOUNT_ITEM, AppConst.UN_AUTH_ACCOUNT_VALUE)
                    .apply()
        }
        authSharedPref!!.registerOnSharedPreferenceChangeListener(this)
        //本地授权标识为空，则跳转到授权界面.
        if (AppConst.UN_AUTH_ACCOUNT_VALUE == authSharedPref!!.getString(AppConst.AUTH_ACCOUNT_ITEM,
                        AppConst.UN_AUTH_ACCOUNT_VALUE)
        ) {
            logger.d("initSharedPref 触发跳转授权界面")
            startAuthView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.authSharedPref!!.unregisterOnSharedPreferenceChangeListener(this)
        unRegisterNetListener()
        logger.d("onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.d("onStartCommand")
        if (intent != null && AppAction.ACTION_BOOT_COMPLETE == intent.action) {
            registerNetLister(this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun registerNetLister(callback: INetConnectedCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (networkCallback == null) {
                networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        logger.d("onAvailable")
                        callback.onConnected()
                    }

                    override fun onLost(network: Network) {
                        logger.d("onLost")
                    }

                    override fun onUnavailable() {
                        logger.d("onUnavailable")
                    }

                    override fun onCapabilitiesChanged(network: Network,
                        networkCapabilities: NetworkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities)
                        logger.d("onCapabilitiesChanged:$networkCapabilities")
                    }
                }
            }
            Objects.requireNonNull(getSystemService(ConnectivityManager::class.java))
                    .registerDefaultNetworkCallback(networkCallback!!)
        } else {
            if (netConnectivityReceiver == null) {
                val filter = IntentFilter()
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                netConnectivityReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        logger.d("onReceive network change " + intent.action!!)
                        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
                            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                                //获取联网状态的NetworkInfo对象
                                val info = intent.getParcelableExtra<NetworkInfo>(
                                        ConnectivityManager.EXTRA_NETWORK_INFO)
                                if (info != null) {
                                    //如果当前的网络连接成功并且网络连接可用
                                    if (NetworkInfo.State.CONNECTED == info.state && info.isAvailable) {
                                        if (info.type == ConnectivityManager.TYPE_WIFI || info.type == ConnectivityManager.TYPE_MOBILE) {
                                            logger.d(info.typeName + " 连上")
                                            callback.onConnected()
                                        }
                                    } else {
                                        logger.d(info.typeName + " 断开")
                                    }
                                }
                            }
                        }
                    }
                }
                registerReceiver(netConnectivityReceiver, filter)
            }
        }
    }

    private fun unRegisterNetListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (networkCallback != null) {
                Objects.requireNonNull(getSystemService(ConnectivityManager::class.java))
                        .unregisterNetworkCallback(networkCallback!!)
            }
        } else {
            if (netConnectivityReceiver != null) {
                unregisterReceiver(netConnectivityReceiver)
                netConnectivityReceiver = null
            }
        }
    }

    @Synchronized override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
        item: String) {
        logger.d("onSharedPreferenceChanged $item")
        if (AppConst.AUTH_ACCOUNT_ITEM == item) {
            // 表示状态由授权变更为未授权
            if (AppConst.UN_AUTH_ACCOUNT_VALUE == sharedPreferences.getString(
                            AppConst.AUTH_ACCOUNT_ITEM,
                            AppConst.UN_AUTH_ACCOUNT_VALUE)
            ) {
                logger.d("监听到未授权变更,触发跳转授权界面")
                startAuthView()
            } else {
                //表示状态由未授权变更为授权
                logger.d("监听到授权变更,触发结束授权界面")
                stopAuthView()
            }
        }
    }

    /**
     * 网络连接后,同步后端的设备授权状态
     */
    override fun onConnected() {
        syncRemoteAuth()
    }

    private fun syncRemoteAuth() {
        logger.d("syncRemoteAuth: 开始同步服务端授权状态")
        iotClient!!.syncAuthorized(this)
    }

    override fun onSyncAuthorized(accountInfo: AccountInfo) {}

    override fun onSyncUnAuthorized() {
        startAuthView()
    }

    override fun onSyncAuthorizedError(e: Exception) {}

    /**
     * 同步QrCode,远端已经授权通过
     *
     * @param accountInfo AccountInfo
     */
    override fun onSyncQrCodeAuthorized(accountInfo: AccountInfo) {}

    /**
     * 同步QrCode
     *
     * @param qrCodeInfo QrCodeInfo
     */
    override fun onSyncQrCodeInfo(qrCodeInfo: QrCodeInfo) {
        if (viewHandler != null) {
            qrCodeInfo.qrCode?.let { viewHandler!!.onShowQrCode(it) }
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
    override fun onSyncQrCodeError(e: Exception) {
        if (viewHandler != null) {
            viewHandler!!.onShowNetError()
        }
    }

    override fun onAuthTimeOut() {
        if (viewHandler != null) {
            viewHandler!!.onShowTimeOut()
        }
    }

    /**
     * 启动授权界面,有且仅当
     * 开机检测本地sharedPref中的授权标记未为授权
     * 监听sharedPref的变更为未授权状态时触发
     */
    private fun startAuthView() {
        logger.d("startAuthView")
        val activityIntent = Intent(this, AuthActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(activityIntent)
    }

    private fun stopAuthView() {
        logger.d("stopAuthView")
        if (viewHandler != null) {
            viewHandler!!.onExit()
        }
    }

    /**
     * view bindService的方式,触发操作service中的syncQrCode
     * @param viewHandler IViewHandler
     */
    override fun registerViewHandler(viewHandler: IViewHandler) {
        logger.d("registerViewHandler")
        this.viewHandler = viewHandler
        this.iotClient!!.syncQrCode(this)
    }

    override fun unRegisterViewHandler(viewHandler: IViewHandler) {
        logger.d("unRegisterViewHandler")
        if (this.viewHandler === viewHandler) {
            this.viewHandler = null
        }
    }

    override fun retryHandler() {
        this.iotClient!!.syncQrCode(this)
    }

    inner class Binder : android.os.Binder() {
        val serviceHandler: IServiceHandler
            get() = this@CoreService
    }

    companion object {
        private val logger = Logger.getLogger("CoreService")

        private const val THREAD_POOL_CORE_SIZE = 4
        private const val THREAD_POOL_MAX_SIZE = 20
        private const val THREAD_POOL_KEEP_ALIVE_TIME = 10
        private val TIME_UNIT = TimeUnit.SECONDS
    }
}
