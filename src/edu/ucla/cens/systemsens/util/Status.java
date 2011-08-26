/**
 * SystemSens
 *
 * Copyright (C) 2011 Hossein Falaki
 */
package edu.ucla.cens.systemsens.util;


import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Formatter;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;


import android.util.Log;




/**
  * This class maintains the current state information collected by
  * SystemSens.
  *
  * @author Hossein Falaki
  */


public class Status
{
    private static String TAG = "StatusObject";


    private static long SECOND = 1000;
    private static long MINUTE = 60 * SECOND;
    private static long HOUR = 60 * MINUTE;



    public static final String LEVEL       = "Battery Level";
    public static final String TEMP        = "Battery Temperature";
    public static final String CPU         = "Average CPU Usage";
    public static final String WIFIRX        = "WiFi RX";
    public static final String WIFITX        = "WiFi TX";
    public static final String CELLRX        = "Cellular RX";
    public static final String CELLTX        = "Cellular TX";
    public static final String EVENTS        = "Screen Events";
    public static final String SCREEN        = "Screen Time";



    private static ArrayList<String> sKeys;

    private static Calendar sDeadline;
    
    private static long sScreenOn = Calendar.getInstance().getTimeInMillis();

    private static boolean sPlugged = false;

    /* Hashtable object that contains status information */
    private static Hashtable<String, Double> sParams;


    private static Status mSelf = null;

    private Status()
    {
        sParams = new Hashtable<String, Double>();
        sKeys = new ArrayList<String>();

        sKeys.add(LEVEL);
        sKeys.add(TEMP);
        sKeys.add(CPU);
        sKeys.add(WIFIRX);
        sKeys.add(WIFITX);
        sKeys.add(CELLRX);
        sKeys.add(CELLTX);
        sKeys.add(EVENTS);
        sKeys.add(SCREEN);

        sDeadline = null;




        for (String key : sKeys)
            sParams.put(key, Double.NaN);


    }

    private static void set(String key, double value)
    {
        if (sParams.containsKey(key))
            sParams.put(key, value);

    }

    private static void setAvg(String key, double value)
    {
        double tempVal = get(key);
        
        if (Double.isNaN(tempVal))
            set(key, value);
        else
            set(key, (get(key) + value)/2.0);
    }

    private static void setTotal(String key, double value)
    {
        Double current = get(key);
        if (current.isNaN())
            current = 0.0;
        double newTotal = value + current;
        set(key, newTotal);
    }

    private static double get(String key)
    {
        if (sParams.containsKey(key))
            return sParams.get(key);
        else
            return Double.NaN;
    }

    private static void check()
    {
        if (mSelf == null)
            mSelf = new Status();
    }




    /**
      * Get methods.
      */

    public static double getLevel()
    {
        check();
        return get(LEVEL);
    }

    public static double getTemp()
    {
        check();
        return get(TEMP);
    }

    public static double getCPU()
    {
        check();
        return get(CPU);
    }

    public static double getWiFi()
    {
        check();
        return get(WIFIRX) + get(WIFITX);
    }

    public static double getCell()
    {
        check();
        return get(CELLRX) + get(CELLTX);
    }

    public static double getDeadline()
    {
        check();
        if (sDeadline == null)
        {
            return Double.NaN;
        }



        Calendar now = Calendar.getInstance();
        double diff = sDeadline.getTimeInMillis() -
            now.getTimeInMillis();


        double res = diff/(1000*60);

        return res;
    }

    /** 
      * Set methods.
      */
    public static void setLevel(double level)
    {
        check();
        set(LEVEL, level);
    }

    public static void setTemp(double temp)
    {
        check();
        set(TEMP, temp);
    }

    public static void setBattery(double level, double temp)
    {
        check();
        setLevel(level);
        setTemp(temp);
    }

    public static void setCPU(double cpu)
    {
        check();
        setAvg(CPU, cpu);
    }

    public static void screenOn()
    {
        check();
        setTotal(EVENTS, 1.0);
        sScreenOn = Calendar.getInstance().getTimeInMillis();
    }


    public static void screenOff()
    {
        check();
        long current = Calendar.getInstance().getTimeInMillis();
        setTotal(SCREEN, current - sScreenOn);
    }



    public static void addWiFiTraffic(double tx, double rx)
    {
        check();
        setTotal(WIFITX, tx);
        setTotal(WIFIRX, rx);
    }

    public static void addCellTraffic(double tx, double rx)
    {
        check();
        setTotal(CELLTX, tx);
        setTotal(CELLRX, rx);
    }


    public static void setWiFiTraffic(double tx, double rx)
    {
        check();
        set(WIFITX, tx);
        set(WIFIRX, rx);
    }

    public static void setCellTraffic(double tx, double rx)
    {
        check();
        set(CELLTX, tx);
        set(CELLRX, rx);
    }

    
    public static void setDeadline(Calendar deadline)
    {
        double curLevel = getLevel();
        //check();
        mSelf = new Status();
        setLevel(curLevel);

        sDeadline = deadline;


   }


    /**
      * Returns the battery deadline as calendar time.
      *
      * @return             String representation of battery deadline
      */
    public static String deadlineStr(double deadline)
    {
        check();

        if (Double.isNaN(deadline))
            return "Not set";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, (int)deadline);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEEEEEE HH:mm",
                Locale.US);

        return sdf.format(cal.getTime());

    }

    public static boolean isPlugged()
    {
        check();
        return sPlugged;
    }


    public static void setPlug(boolean plug)
    {
        check();
        sPlugged = plug;
        if (plug == true)
            sDeadline = null;

    }



    /**
      * Returns a human readabile string of status information.
      *
      * @return             String representation of status
      */
    public static String getString()
    {
        check();

        StringBuilder sbRes = new StringBuilder();
        String key;
        Double val;


        SimpleDateFormat sdf = new SimpleDateFormat("EEEEEEEE HH:mm",
                Locale.US);

        sbRes.append("Battery Goal: ");
        if ((sDeadline != null))
            sbRes.append(sdf.format(sDeadline.getTime()) + "\n");
        else
            sbRes.append("Not set\n");


        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        DecimalFormat intdf = new DecimalFormat();
        intdf.setMinimumIntegerDigits(2);


        Double level = get(LEVEL);
        Double cpu = get(CPU);
        Double wifitx = get(WIFITX);
        Double wifirx = get(WIFIRX);
        Double celltx = get(CELLTX);
        Double cellrx = get(CELLRX);
        Double events = get(EVENTS);
        Double screen = get(SCREEN);



        if (!level.isNaN())
            sbRes.append(LEVEL + ": " + df.format(level) + "%\n");


        if (!cpu.isNaN())
            sbRes.append(CPU + ": " + df.format(cpu) + "%\n");

        if (!events.isNaN())
            sbRes.append(EVENTS + ": " + intdf.format(events) + "\n");

        if (!screen.isNaN())
        {

            long millis = screen.longValue();
            long hour = (long) millis/HOUR;
            long minutes = (long) (millis - hour * HOUR)/MINUTE;
            long seconds = (long)(millis - minutes*MINUTE - hour*HOUR)/SECOND;
            sbRes.append(SCREEN + ": " +
                    intdf.format(hour) + ":" + 
                    intdf.format(minutes) + ":" +
                    intdf.format(seconds) + "\n");
        }




        if (!(wifirx.isNaN() || wifitx.isNaN()) && ((wifitx + wifirx)>0))
            sbRes.append("WiFi (T/R): " 
                    + df.format(wifitx + wifirx)
                    + " (" + df.format(wifitx) + "/" 
                    + df.format(wifirx) + ") MB\n");

        //if (!(celltx.isNaN() || celltx.isNaN()) && ((celltx + cellrx) >0))
        if (!(celltx.isNaN() || celltx.isNaN()))
            sbRes.append("Cellular (T/R): " 
                    +  df.format(celltx + cellrx)
                    + " (" + df.format(celltx) + "/" 
                    + df.format(cellrx) + ") MB\n");



        return sbRes.toString();
    }




}
