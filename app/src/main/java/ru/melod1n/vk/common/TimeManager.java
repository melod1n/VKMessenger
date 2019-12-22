package ru.melod1n.vk.common;


import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimeManager {

    private static int currentHour;
    private static int currentMinute;
    private static int currentSecond;

    private static ArrayList<OnHourChangeListener> onHourChangeListeners = new ArrayList<>();
    private static ArrayList<OnMinuteChangeListener> onMinuteChangeListeners = new ArrayList<>();
    private static ArrayList<OnSecondChangeListener> onSecondChangeListeners = new ArrayList<>();
    private static ArrayList<OnTimeChangeListener> onTimeChangeListeners = new ArrayList<>();

    static void init() {
        Calendar calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR);
        currentMinute = calendar.get(Calendar.MINUTE);
        currentSecond = calendar.get(Calendar.SECOND);

        processThread.start();
    }

    private static Timer timer = new Timer();

    private static Thread processThread = new Thread(() -> timer.schedule(new TimerTask() {
        @Override
        public void run() {
            Date date = new Date(System.currentTimeMillis());

            String sHour = new SimpleDateFormat("HH", Locale.getDefault()).format(date);
            String sMinute = new SimpleDateFormat("mm", Locale.getDefault()).format(date);
            String sSecond = new SimpleDateFormat("ss", Locale.getDefault()).format(date);

            int hour = tryToParseInt(sHour);
            int minute = tryToParseInt(sMinute);
            int second = tryToParseInt(sSecond);

            if (currentHour != hour) {
                currentHour = hour;

                if (onHourChangeListeners != null) {
                    for (OnHourChangeListener onHourChangeListener : onHourChangeListeners)
                        onHourChangeListener.onHourChange(hour);
                }

                if (onTimeChangeListeners != null) {
                    for (OnTimeChangeListener onTimeChangeListener : onTimeChangeListeners)
                        onTimeChangeListener.onHourChange(hour);
                }
            }

            if (currentMinute != minute) {
                currentMinute = minute;

                if (onMinuteChangeListeners != null) {
                    for (OnMinuteChangeListener onMinuteChangeListener : onMinuteChangeListeners)
                        onMinuteChangeListener.onMinuteChange(minute);
                }

                if (onTimeChangeListeners != null) {
                    for (OnTimeChangeListener onTimeChangeListener : onTimeChangeListeners)
                        onTimeChangeListener.onMinuteChange(minute);
                }
            }

            if (currentSecond != second) {
                currentSecond = second;

                if (onSecondChangeListeners != null) {
                    for (OnSecondChangeListener onSecondChangeListener : onSecondChangeListeners)
                        onSecondChangeListener.onSecondChange(second);
                }

                if (onTimeChangeListeners != null) {
                    for (OnTimeChangeListener onTimeChangeListener : onTimeChangeListeners)
                        onTimeChangeListener.onSecondChange(second);
                }
            }

        }
    }, 0, DateUtils.SECOND_IN_MILLIS));

    private static int tryToParseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getCurrentHour() {
        return currentHour;
    }

    public static int getCurrentMinute() {
        return currentMinute;
    }

    public static int getCurrentSecond() {
        return currentSecond;
    }

    public static boolean isMorning() {
        return getCurrentHour() > 6 && getCurrentHour() < 12;
    }

    public static boolean isAfternoon() {
        return getCurrentHour() > 11 && getCurrentHour() < 17;
    }

    public static boolean isEvening() {
        return getCurrentHour() > 16 && getCurrentHour() < 23;
    }

    public static boolean isNight() {
        return getCurrentHour() == 23 || (getCurrentHour() < 6 && getCurrentHour() > -1);
    }

    public static void addOnHourChangeListener(OnHourChangeListener onHourChangeListeners) {
        TimeManager.onHourChangeListeners.add(onHourChangeListeners);
    }

    public static void removeOnHourChangeListener(OnHourChangeListener onHourChangeListener) {
        TimeManager.onHourChangeListeners.remove(onHourChangeListener);
    }

    public static void addOnMinuteChangeListener(OnMinuteChangeListener onMinuteChangeListener) {
        TimeManager.onMinuteChangeListeners.add(onMinuteChangeListener);
    }

    public static void removeOnMinuteChangeListener(OnMinuteChangeListener onMinuteChangeListener) {
        TimeManager.onMinuteChangeListeners.remove(onMinuteChangeListener);
    }

    public static void addOnSecondChangeListener(OnSecondChangeListener onSecondChangeListener) {
        TimeManager.onSecondChangeListeners.add(onSecondChangeListener);
    }

    public static void removeOnSecondChangeListener(OnSecondChangeListener onSecondChangeListener) {
        TimeManager.onSecondChangeListeners.remove(onSecondChangeListener);
    }

    public static void addOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        TimeManager.onTimeChangeListeners.add(onTimeChangeListener);
    }

    public static void removeOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        TimeManager.onTimeChangeListeners.remove(onTimeChangeListener);
    }

    public interface OnHourChangeListener {
        void onHourChange(int currentHour);
    }

    public interface OnMinuteChangeListener {
        void onMinuteChange(int currentMinute);
    }

    public interface OnSecondChangeListener {
        void onSecondChange(int currentSecond);
    }

    public interface OnTimeChangeListener {
        void onHourChange(int currentHour);

        void onMinuteChange(int currentMinute);

        void onSecondChange(int currentSecond);
    }
}