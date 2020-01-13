package com.tim.iot.auth.heartbeat

import com.tim.common.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import okio.ByteString

/**
 * HeartBeatTask
 *
 * @author Tell.Tim
 * @date 2020/1/3 18:23
 */
class HeartBeatTask(private val enable: Boolean, private val interval: Int,
    private val heartBeatHead: ByteString) {
    private var scheduledExecutorService: ScheduledExecutorService? = null

    fun start(callback: IHeartBeatCallback) {
        if (this.enable) {
            reset()
            this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
            try {
                logger.d("开启心跳")
                this.scheduledExecutorService!!.scheduleWithFixedDelay(
                        {
                            logger.d("发送心跳")
                            callback.deal(heartBeatHead)
                        }, 0, this.interval.toLong(), TimeUnit.SECONDS)
            } catch (e: Exception) {
                logger.e("start " + e.message)
            }
        }
    }

    fun stop() {
        reset()
    }

    private fun reset() {
        if (this.enable
                && this.scheduledExecutorService != null
                && !this.scheduledExecutorService!!.isShutdown
        ) {
            try {
                logger.d("心跳复位")
                this.scheduledExecutorService!!.shutdown()
            } catch (e: Exception) {
                try {
                    this.scheduledExecutorService!!.shutdownNow()
                } catch (e1: Exception) {
                    logger.e("reset " + e1.message)
                }
            }

            this.scheduledExecutorService = null
        }
    }

    interface IHeartBeatCallback {
        fun deal(msg: ByteString)
    }

    companion object {
        private val TAG = "HeartBeatTask"
        private val logger = Logger.getLogger(TAG)
    }
}
