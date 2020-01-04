package com.tim.iot.auth.heartbeat;

import com.tim.common.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import okio.ByteString;

/**
 * HeartBeatTask
 *
 * @author Tell.Tim
 * @date 2020/1/3 18:23
 */
public final class HeartBeatTask {
    private static final String TAG = "HeartBeatTask";
    private static final Logger logger = Logger.getLogger(TAG);
    private ScheduledExecutorService scheduledExecutorService;
    private boolean enable;
    private int interval;
    private ByteString heartBeatHead;

    public HeartBeatTask(boolean enableHeartBeat, int heartBeatInterval, ByteString heartBeatHead) {
        this.enable = enableHeartBeat;
        this.interval = heartBeatInterval;
        this.heartBeatHead = heartBeatHead;
    }

    public void start(IHeartBeatCallback callback) {
        if (this.enable) {
            reset();
            this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            try {
                this.scheduledExecutorService.scheduleWithFixedDelay(
                        () -> callback.deal(heartBeatHead), 0, this.interval, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.e("start " + e.getMessage());
            }
        }
    }

    public void stop() {
        reset();
    }

    private void reset() {
        if (this.enable
                && this.scheduledExecutorService != null
                && !this.scheduledExecutorService.isShutdown()) {
            try {
                this.scheduledExecutorService.shutdown();
            } catch (Exception e) {
                try {
                    this.scheduledExecutorService.shutdownNow();
                } catch (Exception e1) {
                    logger.e("reset " + e1.getMessage());
                }
            }
            this.scheduledExecutorService = null;
        }
    }

    public interface IHeartBeatCallback {
        void deal(ByteString msg);
    }
}
