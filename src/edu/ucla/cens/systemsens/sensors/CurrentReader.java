package edu.ucla.cens.systemsens.sensors;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.File;

import android.util.Log;
import android.os.Build;

/**
 * Reads the file corresponding to smart battery current values on the
 * /sys of Linux.
 * 
 * This code is mostly borrowed from the currentwidget project
 *
 * @author Hossein Falaki
 */
public class CurrentReader
{
	private static final String TAG = "CurrentReader";

    // List of model names
    private static final String VIBRANT = "sgh-t959";
    private static final String NEXUSONE = "nexus one";

    private File mFile;
    private boolean mReadCurrent = true;


    public CurrentReader()
    {
        String model = Build.MODEL.toLowerCase();


        // htc desire hd / desire z / inspire?
        if (Build.MODEL.toLowerCase().contains("desire hd") || 
                Build.MODEL.toLowerCase().contains("desire z") || 
                Build.MODEL.toLowerCase().contains("inspire")) 
        { 
            mFile = 
                new File("/sys/class/power_supply/battery/batt_current"); 

            if (mFile.exists()) 
                return; 
        }
        // nexus one cyangoenmod
        mFile = new File("/sys/devices/platform/ds2784-battery/getcurrent");
        if (mFile.exists()) 
            return;

        // sony ericsson xperia x1
        mFile = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/ds2746-battery/current_now");
        if (mFile.exists()) 
            return;

        // xdandroid
        mFile = new
            File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/battery/current_now");
        if (mFile.exists()) 
            return;


        // droid eris
        mFile = new File("/sys/class/power_supply/battery/smem_text");
        if (mFile.exists()) 
            return;

        // some HTC devices
        mFile = new
            File("/sys/class/power_supply/battery/batt_current");
        if (mFile.exists()) 
            return;


        // samsung galaxy vibrant       
        mFile = new File("/sys/class/power_supply/battery/batt_chg_current");
        if (mFile.exists()) 
            return;


        // sony ericsson x10
        mFile = new
            File("/sys/class/power_supply/battery/charger_current");
        if (mFile.exists()) 
            return;


        // Nook Color
        mFile = new File("/sys/class/power_supply/max17042-0/current_now");
        if (mFile.exists()) 
            return;


        Log.i(TAG, "Could not check current file");
        mReadCurrent = false;


    }


    public long getCurrent()
    {
        if (mReadCurrent)
        {
            String currentText = null;
            try
            {
                FileInputStream inputFile = new FileInputStream(mFile);
                DataInputStream dataStream = new DataInputStream(inputFile);

                currentText = dataStream.readLine();

            }
            catch (Exception e)
            {
                Log.e(TAG, "Could not read current file", e);
            }

            long current = -1L;

            if (currentText != null)
            {
                try
                {
                    current = Long.parseLong(currentText);
                }
                catch (NumberFormatException nfe)
                {
                    Log.e(TAG, "Could not parse current value", nfe);
                }
            }

            Log.i(TAG, "Successfully read current: " + current);
        
            return current;

        }
        else
        {
            return -1L;
        }



    }
}
