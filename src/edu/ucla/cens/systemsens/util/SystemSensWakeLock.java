/** 
  *
  * Copyright (c) 2011, The Regents of the University of California. All
  * rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *   * Redistributions of source code must retain the above copyright
  *   * notice, this list of conditions and the following disclaimer.
  *
  *   * Redistributions in binary form must reproduce the above copyright
  *   * notice, this list of conditions and the following disclaimer in
  *   * the documentation and/or other materials provided with the
  *   * distribution.
  *
  *   * Neither the name of the University of California nor the names of
  *   * its contributors may be used to endorse or promote products
  *   * derived from this software without specific prior written
  *   * permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT
  * HOLDER> BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
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

