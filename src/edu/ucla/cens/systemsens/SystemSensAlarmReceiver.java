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

