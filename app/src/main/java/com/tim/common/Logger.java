package com.tim.common;

import android.util.Log;

/**
 * Logger
 *
 * @author Tell.Tim
 * @date 2019/12/24 14:22
 */
public class Logger {
    private static final String MAIN_TAG = "Iot";

    public static Logger getLogger(String subTag) {
        return new Logger(subTag);
    }

    private final String subTag;

    private Logger(String subTag) {
        this.subTag = subTag;
    }

    public void dFormat(String format, Object... objects) {
        String msg = String.format(format, objects);
        Log.d(MAIN_TAG, subTag + ":" + msg);
    }

    public void d(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object o : objects) {
            builder.append(" --> ").append(o);
        }

        Log.d(MAIN_TAG, subTag + ":" + builder.toString());
    }

    public void eFormat(String format, Object... objects) {
        String msg = String.format(format, objects);
        Log.e(MAIN_TAG, subTag + ":" + msg);
    }

    public void e(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object o : objects) {
            builder.append(" --> ").append(o);
        }
        Log.e(MAIN_TAG, subTag + ":" + builder.toString());
    }
}
