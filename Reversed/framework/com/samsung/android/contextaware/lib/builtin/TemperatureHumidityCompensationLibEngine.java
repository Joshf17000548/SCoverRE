package com.samsung.android.contextaware.lib.builtin;

public class TemperatureHumidityCompensationLibEngine {
    static {
        System.load("/system/lib/libsensorhubcontext.so");
    }

    public native void native_temperaturehumidity_getCompensatedData(double d, double d2, double[] dArr, double[] dArr2, long j);

    public native void native_temperaturehumidity_getLastCompensatedData(double[] dArr, double[] dArr2);
}
