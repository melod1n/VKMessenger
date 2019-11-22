package ru.melod1n.vk.concurrent;

public class TaskManager {

    public static void execute(Runnable runnable) {
        new LowThread(runnable).start();
    }

}
