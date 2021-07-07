package com.github.salilvnair.excelprocessor.v2.helper;

/**
 * @author Salil V Nair
 */
public class StopWatch {
    static long startTime;
    static long stopTime;
    public static void start() {
        startTime = System.currentTimeMillis();
    }
    public static long stop() {
        stopTime = System.currentTimeMillis();
        return stopTime - startTime;
    }
}
