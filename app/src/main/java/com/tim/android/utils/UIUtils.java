package com.tim.android.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author Tell.Tim
 * @program HomeBox
 * @packageName com.hunter.common.utils
 * @fileName UIUtils
 * @date 2019/9/10 13:06
 * @description
 */
public class UIUtils {
    /**
     * dip-->px
     */
    public static int dip2Px(Context context, int dip) {
        // px/dip = density;
        // density = dpi/160
        // 320*480 density = 1 1px = 1dp
        // 1280*720 density = 2 2px = 1dp

        float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + 0.5f);
        return px;
    }

    /**
     * px-->dip
     */
    public static int px2dip(Context context, int px) {

        float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
        int dip = (int) (px / density + 0.5f);
        return dip;
    }

    /**
     * sp-->px
     */
    public static int sp2px(Context context, int sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                context.getApplicationContext().getResources().getDisplayMetrics()) + 0.5f);
    }
}
