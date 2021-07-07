package com.github.salilvnair.excelprocessor.util;

import java.util.concurrent.TimeUnit;

/**
 * @author Salil V Nair
 */
public class StopWatch {
    static long startTime;
    static long stopTime;
    static long durationInMillis;
    public static void start() {
        startTime = System.currentTimeMillis();
    }
    public static long stop() {
        stopTime = System.currentTimeMillis();
        durationInMillis =  stopTime - startTime;
        return durationInMillis;
    }

    public static long elapsed(TimeUnit timeUnit) {
        TimeUnit time = TimeUnit.MILLISECONDS;
        stop();
        return timeUnit.convert(durationInMillis, time);
    }
}
