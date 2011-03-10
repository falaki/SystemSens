/**
 * SystemSens
 *
 * Copyright (C) 2009 Hossein Falaki
 */

package edu.ucla.cens.systemsens.util;


import android.content.Context;
import android.os.PowerManager;
import android.util.Log;


import java.lang.Thread;



/**
 * Manages a static WakeLock to gaurantee that the phone
 * does not go to sleep before SystemSens Service is started
 * by the Alarm BroadcastReceiver.
 * 
 *
 * @author      Hossein Falaki
 */
public class SystemSensWakeLock 
{

    private static final String TAG = "SystemSensWakeLock";

    private static PowerManager.WakeLock sCpuWakeLock;


    public static void acquireCpuWakeLock(Context context)
    {
        Log.i(TAG, "Acquiring cpu wake lock");

        if (sCpuWakeLock != null)
            return;


        PowerManager pm = (PowerManager) context.getSystemService(
                context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, 
                "SystemTempWake");

        sCpuWakeLock.acquire();

    }


    public static void releaseCpuLock()
    {
        Log.i(TAG, "Releaseing cpu wake lock");

        if (sCpuWakeLock != null)
        {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }




}

