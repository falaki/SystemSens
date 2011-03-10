/**
 * SystemSensLite
 *
 * Copyright (C) 2009 Hossein Falaki
 */

package edu.ucla.cens.systemsens;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



/**
 * Starts the SystemSens Service at boot time.
 *
 * @author      Hossein Falaki
 */
public class SystemSensStartup extends BroadcastReceiver 
{

    private static final String TAG = "SystemSensStartup";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        context.startService(new Intent(context, 
                    SystemSens.class));
        Log.i(TAG, "Started SystemSens");

    }

}

