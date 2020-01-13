package com.tim.android.activity

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.tim.android.service.CoreService
import com.tim.android.service.IServiceHandler
import com.tim.android.service.IViewHandler
import com.tim.common.Logger
import com.tim.android.utils.UIUtils
import com.tim.iot.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * AuthActivity
 *
 * @author Tell.Tim
 * @date 2019/12/24 19:45
 */
class AuthActivity : AppCompatActivity(), ServiceConnection, IViewHandler {
    private var serviceHandler: IServiceHandler? = null
    internal var imgQrCode: ImageView
    internal var pbQrCode: ProgressBar
    internal var tvErrorMsg: TextView
    internal var lLayoutErrorMsg: LinearLayout
    private var subscribe: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        imgQrCode = findViewById(R.id.img_qrcode)
        pbQrCode = findViewById(R.id.qr_code_progress)
        tvErrorMsg = findViewById(R.id.tv_error)
        lLayoutErrorMsg = findViewById(R.id.error_field)
        logger.d("onCreate $taskId")
        bindService(Intent(this, CoreService::class.java), this, Service.BIND_WAIVE_PRIORITY)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logger.d("onNewIntent")
    }

    override fun onResume() {
        super.onResume()
        logger.d("onResume")
    }

    override fun onPause() {
        super.onPause()
        logger.d("onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this.subscribe != null && !this.subscribe!!.isDisposed) {
            this.subscribe!!.dispose()
        }
        if (serviceHandler != null) {
            serviceHandler!!.unRegisterViewHandler(this)
        }
        unbindService(this)
        logger.d("onDestroy")
    }

    override fun onBackPressed() {
        //屏蔽返回键
    }

    override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
        serviceHandler = (binder as CoreService.Binder).serviceHandler
        serviceHandler!!.registerViewHandler(this)
        tvErrorMsg.setOnClickListener { view ->
            //todo
            pbQrCode.visibility = View.VISIBLE
            lLayoutErrorMsg.visibility = View.GONE
            serviceHandler!!.retryHandler()
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
    }

    override fun onShowQrCode(qrCode: String) {
        if (!TextUtils.isEmpty(qrCode)) {
            subscribe = Observable.just<Bitmap>(
                    QRCodeEncoder.syncEncodeQRCode(qrCode, UIUtils.dip2Px(this, 200)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ bitmap ->
                        imgQrCode.visibility = View.VISIBLE
                        imgQrCode.setImageBitmap(bitmap)
                        pbQrCode.visibility = View.GONE
                        lLayoutErrorMsg.visibility = View.GONE
                    }, { throwable -> logger.e("error " + throwable.localizedMessage!!) })
        }
    }

    override fun onShowTimeOut() {
        runOnUiThread {
            imgQrCode.visibility = View.GONE
            pbQrCode.visibility = View.GONE
            lLayoutErrorMsg.visibility = View.VISIBLE
            tvErrorMsg.text = "授权超时,请重新获取二维码"
        }
    }

    override fun onShowNetError() {
        runOnUiThread {
            imgQrCode.visibility = View.GONE
            pbQrCode.visibility = View.GONE
            lLayoutErrorMsg.visibility = View.VISIBLE
            tvErrorMsg.text = "网络异常,请稍后重试"
        }
    }

    override fun onExit() {
        logger.d("授权通过,退出界面")
        finish()
    }

    companion object {
        private val TAG = "AuthActivity"
        private val logger = Logger.getLogger(TAG)
    }
}
