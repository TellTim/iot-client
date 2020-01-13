package com.tim.android.utils

import android.content.Context
import android.util.TypedValue

/**
 * @author Tell.Tim
 * @program HomeBox
 * @packageName com.hunter.common.utils
 * @fileName UIUtils
 * @date 2019/9/10 13:06
 * @description
 */
object UIUtils {
    /**
     * dip-->px
     */
    fun dip2Px(context: Context, dip: Int): Int {
        // px/dip = density;
        // density = dpi/160
        // 320*480 density = 1 1px = 1dp
        // 1280*720 density = 2 2px = 1dp

        val density = context.applicationContext.resources.displayMetrics.density
        return (dip * density + 0.5f).toInt()
    }

    /**
     * px-->dip
     */
    fun px2dip(context: Context, px: Int): Int {

        val density = context.applicationContext.resources.displayMetrics.density
        return (px / density + 0.5f).toInt()
    }

    /**
     * sp-->px
     */
    fun sp2px(context: Context, sp: Int): Int {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(),
                context.applicationContext.resources.displayMetrics) + 0.5f).toInt()
    }
}
