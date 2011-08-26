/**
 * SystemSens
 *
 * Copyright (C) 2009 Hossein Falaki
 */

package edu.ucla.cens.systemsens;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import java.lang.Thread;


import edu.ucla.cens.systemsens.util.SystemSensWakeLock;


/**
 * Logs polling sensors.
 *
 * @author      Hossein Falaki
 */
public class SystemSensAlarmReceiver extends BroadcastReceiver 
{

    private static final String TAG = "SystemSensAlarmReceiver";


    @Override
    public void onReceive(Context context, Intent intent)
    {

        // Acquire a lock
        SystemSensWakeLock.acquireCpuWakeLock(context);

        Intent newIntent = new Intent(context, SystemSens.class);
        newIntent.setAction(SystemSens.POLLSENSORS_ACTION);

        context.startService(newIntent);


    }

}

