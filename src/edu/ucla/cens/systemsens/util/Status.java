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




/**
  * This class maintains the current state information collected by
  * SystemSens.
  *
  * @author Hossein Falaki
  */


public class Status
{
    private static String TAG = "StatusObject";


    public static final String LEVEL       = "Battery Level (%)";
    public static final String TEMP        = "Battery Temperature (C)";
    public static final String CPU         = "Average CPU usage (%)";
    public static final String RX          = "Total Received (MB)";
    public static final String TX          = "Total Sent (MB)";



    private static ArrayList<String> sKeys;

    private static Calendar sDeadline;

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
        sKeys.add(TX);
        sKeys.add(RX);

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
        double newTotal = value + get(key);
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

    public static double getRx()
    {
        check();
        return get(RX);
    }

    public static double getTx()
    {
        check();
        return get(TX);
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

    public static void addTraffic(double tx, double rx)
    {
        check();
        setTotal(TX, tx);
        setTotal(RX, rx);
    }

    public static void setTraffic(double tx, double rx)
    {
        check();
        set(TX, tx);
        set(RX, rx);
    }
    
    public static void setDeadline(double minutes)
    {
        check();
        sDeadline = Calendar.getInstance();
        sDeadline.add(Calendar.MINUTE, (int)minutes);


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

        sbRes.append("Battery deadline: ");
        if (sDeadline != null)
            sbRes.append(sdf.format(sDeadline.getTime()) + "\n");
        else
            sbRes.append("Not set\n");


        DecimalFormat df = new DecimalFormat("@@##");



        for (int i=0; i < sKeys.size(); i++)
        {
            key = sKeys.get(i);
            val = get(key);
            sbRes.append(key + ": ");

            if (val.isNaN())
                sbRes.append("Not set\n");
            else 
                sbRes.append(df.format(val) + "\n");
        }

        return sbRes.toString();
    }




}
