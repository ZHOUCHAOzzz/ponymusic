package me.wcy.music.service

import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import com.blankj.utilcode.util.AppUtils

/**
 * Created by hzwangchenyan on 2017/8/8.
 */
object QuitTimer {
    private var listener: OnTimerListener? = null
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var timerRemain: Long = 0

    fun setOnTimerListener(listener: OnTimerListener?) {
        this.listener = listener
    }

    fun start(milli: Long) {
        stop()
        if (milli > 0) {
            timerRemain = milli + DateUtils.SECOND_IN_MILLIS
            handler.post(mQuitRunnable)
        } else {
            timerRemain = 0
            listener?.onTimer(timerRemain)
        }
    }

    private fun stop() {
        handler.removeCallbacks(mQuitRunnable)
    }

    private val mQuitRunnable: Runnable = object : Runnable {
        override fun run() {
            timerRemain -= DateUtils.SECOND_IN_MILLIS
            if (timerRemain > 0) {
                listener?.onTimer(timerRemain)
                handler.postDelayed(this, DateUtils.SECOND_IN_MILLIS)
            } else {
                AppUtils.exitApp()
            }
        }
    }

    interface OnTimerListener {
        /**
         * 更新定时停止播放时间
         */
        fun onTimer(remain: Long)
    }
}