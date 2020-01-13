package com.tim.common

import android.util.Log

/**
 * Logger
 *
 * @author Tell.Tim
 * @date 2019/12/24 14:22
 */
class Logger private constructor(private val subTag: String) {

    fun dFormat(format: String, vararg objects: Any) {
        val msg = String.format(format, *objects)
        Log.d(MAIN_TAG, "$subTag:$msg")
    }

    fun d(vararg objects: Any) {
        val builder = StringBuilder()
        for (o in objects) {
            builder.append(" --> ").append(o)
        }

        Log.d(MAIN_TAG, "$subTag:$builder")
    }

    fun eFormat(format: String, vararg objects: Any) {
        val msg = String.format(format, *objects)
        Log.e(MAIN_TAG, "$subTag:$msg")
    }

    fun e(vararg objects: Any) {
        val builder = StringBuilder()
        for (o in objects) {
            builder.append(" --> ").append(o)
        }
        Log.e(MAIN_TAG, "$subTag:$builder")
    }

    companion object {
        private val MAIN_TAG = "Iot"

        fun getLogger(subTag: String): Logger {
            return Logger(subTag)
        }
    }
}
