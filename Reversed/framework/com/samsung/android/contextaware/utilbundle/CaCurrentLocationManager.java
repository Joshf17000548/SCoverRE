package com.samsung.android.contextaware.utilbundle;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.samsung.android.contextaware.manager.ICurrrentLocationObserver;
import com.samsung.android.contextaware.utilbundle.logger.CaLogger;
import com.samsung.android.location.SemLocationManager;

public class CaCurrentLocationManager implements ICurrentLocationObservable, IUtilManager {
    private static final float GPS_MIN_DISTANCE = 0.0f;
    private static final long GPS_MIN_TIME = 1000;
    private Criteria mCriteria;
    private boolean mEnable = false;
    private LocationManager mGpsManager;
    private ICurrrentLocationObserver mListener;
    private final LocationListener mLocationListener = new C00171();
    private final Looper mLooper;

    class C00171 implements LocationListener {
        C00171() {
        }

        public final void onLocationChanged(Location location) {
            if (location.getAccuracy() <= CaCurrentLocationManager.GPS_MIN_DISTANCE) {
                CaLogger.warning("Accuracy is low");
                return;
            }
            CaCurrentLocationManager.this.notifyCurrentLocationObserver(System.currentTimeMillis(), location.getTime(), location.getLatitude(), location.getLongitude(), location.getAltitude());
        }

        public final void onProviderDisabled(String str) {
            CaLogger.info("Location service is disabled");
        }

        public final void onProviderEnabled(String str) {
            CaLogger.info("Location service is enabled");
        }

        public final void onStatusChanged(String str, int i, Bundle bundle) {
            switch (i) {
                case 0:
                    CaLogger.info("out of service");
                    return;
                case 1:
                    CaLogger.info("temporarily unavailable");
                    return;
                case 2:
                    CaLogger.info("available");
                    return;
                default:
                    return;
            }
        }
    }

    class C00193 implements Runnable {
        C00193() {
        }

        public void run() {
            CaCurrentLocationManager.this.mGpsManager.removeUpdates(CaCurrentLocationManager.this.mLocationListener);
        }
    }

    public CaCurrentLocationManager(Context context, Looper looper, ICurrrentLocationObserver iCurrrentLocationObserver) {
        this.mLooper = looper;
        initializeManager(context);
        registerCurrentLocationObserver(iCurrrentLocationObserver);
    }

    public void disable() {
        if (this.mGpsManager == null) {
            CaLogger.error("cannot unregister the gps listener");
        } else if (this.mEnable) {
            CaLogger.trace();
            new Handler(this.mLooper).postDelayed(new C00193(), 0);
            this.mEnable = false;
        }
    }

    public final void enable() {
        if (this.mGpsManager == null) {
            CaLogger.error("mGpsManager is null");
        } else if (this.mLooper == null) {
            CaLogger.error("Looper is null");
        } else {
            final String bestProvider = this.mGpsManager.getBestProvider(this.mCriteria, true);
            if (bestProvider == null || bestProvider.isEmpty()) {
                CaLogger.error("cannot register the gps listener");
                return;
            }
            if (this.mEnable) {
                CaLogger.warning("mEnable value is true.");
                disable();
            }
            this.mEnable = true;
            CaLogger.info("BestProvider : " + bestProvider);
            new Handler(this.mLooper).postDelayed(new Runnable() {
                public void run() {
                    CaCurrentLocationManager.this.mGpsManager.requestLocationUpdates(bestProvider, CaCurrentLocationManager.GPS_MIN_TIME, CaCurrentLocationManager.GPS_MIN_DISTANCE, CaCurrentLocationManager.this.mLocationListener, CaCurrentLocationManager.this.mLooper);
                }
            }, 0);
        }
    }

    public final void initializeManager(Context context) {
        this.mCriteria = new Criteria();
        this.mCriteria.setAccuracy(1);
        this.mCriteria.setPowerRequirement(2);
        this.mCriteria.setAltitudeRequired(true);
        this.mCriteria.setBearingRequired(false);
        this.mCriteria.setSpeedRequired(false);
        this.mCriteria.setCostAllowed(true);
        this.mGpsManager = (LocationManager) context.getSystemService(SemLocationManager.GEOFENCE_LOCATION);
        if (this.mGpsManager == null) {
            CaLogger.error("cannot create the GpsManager object");
        }
        this.mEnable = false;
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public final void notifyCurrentLocationObserver(long j, long j2, double d, double d2, double d3) {
        if (this.mListener != null) {
            this.mListener.updateCurrentLocation(j, j2, d, d2, d3);
        }
        disable();
    }

    public final void registerCurrentLocationObserver(ICurrrentLocationObserver iCurrrentLocationObserver) {
        this.mListener = iCurrrentLocationObserver;
    }

    public final void terminateManager() {
        if (this.mGpsManager == null) {
            CaLogger.error("mGpsManager is null");
        } else {
            this.mGpsManager.removeUpdates(this.mLocationListener);
        }
    }

    public final void unregisterCurrentLocationObserver() {
        this.mListener = null;
    }
}
