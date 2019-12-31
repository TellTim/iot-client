package com.tim.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.tim.android.constant.AppAction;
import com.tim.android.service.CoreService;
import com.tim.common.Logger;
import java.util.Objects;

/**
 * BootReceiver
 *
 * @author Tell.Tim
 * @date 2019/12/24 12:59
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Logger.getLogger("BootReceiver").d("Receive boot complete");
            Intent service = new Intent(context, CoreService.class);
            service.setAction(AppAction.ACTION_BOOT_COMPLETE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        }
    }
}
