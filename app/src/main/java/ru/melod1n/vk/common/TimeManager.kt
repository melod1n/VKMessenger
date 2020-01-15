package ru.melod1n.vk.common

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object TimeManager {
    var currentHour = 0
        private set
    var currentMinute = 0
        private set
    var currentSecond = 0
        private set
    private val onHourChangeListeners: ArrayList<OnHourChangeListener>? = ArrayList()
    private val onMinuteChangeListeners: ArrayList<OnMinuteChangeListener>? = ArrayList()
    private val onSecondChangeListeners: ArrayList<OnSecondChangeListener>? = ArrayList()
    private val onTimeChangeListeners: ArrayList<OnTimeChangeListener>? = ArrayList()
    fun init() {
        val calendar = Calendar.getInstance()
        currentHour = calendar[Calendar.HOUR]
        currentMinute = calendar[Calendar.MINUTE]
        currentSecond = calendar[Calendar.SECOND]
        processThread.start()
    }

    private val timer = Timer()
    private val processThread = Thread(Runnable {
        timer.schedule(object : TimerTask() {
            override fun run() {
                val date = Date(System.currentTimeMillis())
                val sHour = SimpleDateFormat("HH", Locale.getDefault()).format(date)
                val sMinute = SimpleDateFormat("mm", Locale.getDefault()).format(date)
                val sSecond = SimpleDateFormat("ss", Locale.getDefault()).format(date)
                val hour = tryToParseInt(sHour)
                val minute = tryToParseInt(sMinute)
                val second = tryToParseInt(sSecond)
                AppGlobal.handler.post {
                    if (currentHour != hour) {
                        currentHour = hour
                        if (onHourChangeListeners != null) {
                            for (onHourChangeListener in onHourChangeListeners) onHourChangeListener.onHourChange(hour)
                        }
                        if (onTimeChangeListeners != null) {
                            for (onTimeChangeListener in onTimeChangeListeners) onTimeChangeListener.onHourChange(hour)
                        }
                    }
                    if (currentMinute != minute) {
                        currentMinute = minute
                        if (onMinuteChangeListeners != null) {
                            for (onMinuteChangeListener in onMinuteChangeListeners) onMinuteChangeListener.onMinuteChange(minute)
                        }
                        if (onTimeChangeListeners != null) {
                            for (onTimeChangeListener in onTimeChangeListeners) onTimeChangeListener.onMinuteChange(minute)
                        }
                    }
                    if (currentSecond != second) {
                        currentSecond = second
                        if (onSecondChangeListeners != null) {
                            for (onSecondChangeListener in onSecondChangeListeners) onSecondChangeListener.onSecondChange(second)
                        }
                        if (onTimeChangeListeners != null) {
                            for (onTimeChangeListener in onTimeChangeListeners) onTimeChangeListener.onSecondChange(second)
                        }
                    }
                }
            }
        }, 0, DateUtils.SECOND_IN_MILLIS)
    })

    private fun tryToParseInt(string: String): Int {
        return try {
            string.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    val isMorning: Boolean
        get() = currentHour > 6 && currentHour < 12

    val isAfternoon: Boolean
        get() = currentHour > 11 && currentHour < 17

    val isEvening: Boolean
        get() = currentHour > 16 && currentHour < 23

    val isNight: Boolean
        get() = currentHour == 23 || currentHour < 6 && currentHour > -1

    fun addOnHourChangeListener(onHourChangeListeners: OnHourChangeListener) {
        TimeManager.onHourChangeListeners!!.add(onHourChangeListeners)
    }

    fun removeOnHourChangeListener(onHourChangeListener: OnHourChangeListener?) {
        onHourChangeListeners!!.remove(onHourChangeListener)
    }

    fun addOnMinuteChangeListener(onMinuteChangeListener: OnMinuteChangeListener) {
        onMinuteChangeListeners!!.add(onMinuteChangeListener)
    }

    fun removeOnMinuteChangeListener(onMinuteChangeListener: OnMinuteChangeListener?) {
        onMinuteChangeListeners!!.remove(onMinuteChangeListener)
    }

    fun addOnSecondChangeListener(onSecondChangeListener: OnSecondChangeListener) {
        onSecondChangeListeners!!.add(onSecondChangeListener)
    }

    fun removeOnSecondChangeListener(onSecondChangeListener: OnSecondChangeListener?) {
        onSecondChangeListeners!!.remove(onSecondChangeListener)
    }

    fun addOnTimeChangeListener(onTimeChangeListener: OnTimeChangeListener) {
        onTimeChangeListeners!!.add(onTimeChangeListener)
    }

    fun removeOnTimeChangeListener(onTimeChangeListener: OnTimeChangeListener?) {
        onTimeChangeListeners!!.remove(onTimeChangeListener)
    }

    interface OnHourChangeListener {
        fun onHourChange(currentHour: Int)
    }

    interface OnMinuteChangeListener {
        fun onMinuteChange(currentMinute: Int)
    }

    interface OnSecondChangeListener {
        fun onSecondChange(currentSecond: Int)
    }

    interface OnTimeChangeListener {
        fun onHourChange(currentHour: Int)
        fun onMinuteChange(currentMinute: Int)
        fun onSecondChange(currentSecond: Int)
    }
}