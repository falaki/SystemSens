/** SystemSens
  *
  * Copyright (C) 2010 Center for Embedded Networked Sensing
  */

package edu.ucla.cens.systemsens;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Build;
import android.os.Vibrator;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.database.SQLException;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;


import org.json.JSONObject;
import org.json.JSONException;

import edu.ucla.cens.systemsens.receivers.PhoneStateReceiver;
import edu.ucla.cens.systemsens.sensors.Proc;
import edu.ucla.cens.systemsens.sensors.EventLogger;
import edu.ucla.cens.systemsens.sensors.NetLogger;
import edu.ucla.cens.systemsens.util.SystemSensDbAdaptor;
import edu.ucla.cens.systemsens.util.SystemSensWakeLock;
import edu.ucla.cens.systemsens.util.Uploader;
import edu.ucla.cens.systemsens.util.Status;



/**
 * SystemSensLite runs as an Android service.
 * It collects system information and stores in a local file.
 * 
 *  @author Hossein Falaki
 */
public class SystemSens extends Service
{

    /** Name of the service used for logging */
    private static final String TAG = "SystemSens";

    /** Version of the JSON records */
    public static final String VER = "3.0";

    /** Action string for recording polling sensors */
    public static final String POLLSENSORS_ACTION = "PollSENSORS";


    /** Types of messages used by this service */
    private static final int USAGESTAT_MSG    = 1;
    private static final int UPLOAD_START_MSG = 2;
    private static final int UPLOAD_END_MSG   = 3;
    private static final int BATHIST_MSG      = 4;
    private static final int PROC_MSG         = 5;
    private static final int WIFISCAN_MSG     = 6;
    private static final int EVENTLOG_MSG	  = 7;
    private static final int NETLOG_MSG       = 8;

    /** String names of JSON records */
    public static final String USAGESTAT_TYPE = "usage";
    public static final String NETTRANS_TYPE = "transmission";
    public static final String NETRECV_TYPE = "receive";
    public static final String CPUSTAT_TYPE = "cpu";
    public static final String MEMSTAT_TYPE = "memory";
    public static final String MEMINFO_TYPE = "meminfo";
    public static final String GPSSTAT_TYPE = "gps";
    public static final String GPSSTATE_TYPE = "gpsstate";
    public static final String SENSORSTAT_TYPE = "sensor";
    public static final String BATTERY_TYPE = "battery";
    public static final String SCREEN_TYPE = "screen";
    public static final String NET_TYPE = "network";
    public static final String CALL_TYPE = "call";
    public static final String SYSTEMSENS_TYPE = "systemsens";
    public static final String NETDEV_TYPE = "netdev";
    public static final String NETLOG_TYPE = "netlog";
    public static final String NETIFLOG_TYPE = "netiflog";
    public static final String ACTIVITYLOG_TYPE = "activitylog";
    public static final String SERVICELOG_TYPE = "servicelog";
    public static final String WIFISCAN_TYPE = "wifiscan";


    /** String names of JSON data keys */
    public static final String BATTERY_LEVEL = "level";
    public static final String BATTERY_TEMP = "temperture";





    /** Intervals used for timers in seconds */
    private long POLLING_INTERVAL;
    private long WIFISCAN_INTERVAL;


    /** Unites of time */
    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = 60 * ONE_SECOND;

    
    /** Default values for timers in seconds */
    private static final long DEFAULT_POLLING_INTERVAL = 2 * ONE_MINUTE;
    private static final long DEFAULT_WIFISCAN_INTERVAL = 2 * ONE_MINUTE;
   
    
    private static final int MIN_GPS_TIME = 10 * ONE_MINUTE;
    private static final int MIN_GPS_DIST = 100;



    /** Unique identifier for the notification */
    private static final int NOTIFICATION_ID = 0;


   
    /** Location manager to get GPS information */
    /* Consumes too much power */
    //private LocationManager mLocManager;

    /** Power manager object used to acquire a partial wakeLock */
    private PowerManager mPM;


    /** Vibrator service */
    Vibrator mVib;
 

    /** WakeLock object */
    private PowerManager.WakeLock mWL;


    /** Notification manager object */
    private NotificationManager mNM;



    /** State variable set when a worker thread starts uploading */
    private boolean mIsUploading;

    /** Uploader Object */
    private Uploader mUploader;

    /** Dumper Object - used for debugging */
    //private Dumper mDumper;


    /** Database adaptor object */
    private SystemSensDbAdaptor mDbAdaptor;

    /** Holds the IMEI of the device */
    public static String IMEI;


    /** telephonyManager object */
    private TelephonyManager mTelManager;
    
    /** Connectivity Manager object */
    private ConnectivityManager mConManager;


    /** WifiManager object for scanning */
    private WifiManager mWifi;

    /** Proc parser object */
    private Proc mProc;
    
    /** EventLogger object */
    private EventLogger mEventLogger;
    
    /** NetLogger object */
    private NetLogger mNetLogger;
    
    /** Receiver objects */
    private PhoneStateReceiver mStateListener;
    
    /** Flag set when WiFi scanning is running */
    private boolean mIsScanning = false;

    /** Flag set when the phone is plugged */
    private static boolean mIsPlugged = false;


    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        handleStart(intent);
        
    }


    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        handleStart(intent);
        return START_STICKY;
        
    }
    */


    private void handleStart(Intent intent)
    {
        if (intent != null)
        {
            String action = intent.getAction();
            
            if (action != null)
                if (action.equals(POLLSENSORS_ACTION))
                {
                    pollingSensors();

                }
        }

    }

    
    /**
      * All initializations happen here. 
      *
      */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        
        



        // Set the default intervals 
        POLLING_INTERVAL = DEFAULT_POLLING_INTERVAL;

        WIFISCAN_INTERVAL = DEFAULT_WIFISCAN_INTERVAL;

        

        /* Get manager objects from the system */
        mTelManager = (TelephonyManager)this.getSystemService(
                Context.TELEPHONY_SERVICE);
        mConManager = (ConnectivityManager) this.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        IMEI = mTelManager.getDeviceId(); 

        mIsUploading = false;

        mDbAdaptor = new SystemSensDbAdaptor(this);

        mUploader = new Uploader(mDbAdaptor);
        //mDumper = new Dumper(mDbAdaptor, this);

        

        mProc = new Proc();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO )
        {
            mEventLogger = new EventLogger();
            mNetLogger = new NetLogger(this);
        }



        /* Obsolete. Now the databas is not kept open continiously
        try
        {
            mDbAdaptor.open();
        }
        catch (SQLException e)
        {
            Log.e(TAG, "Exception", e);
        }
        */

        // Register for battery updates
        registerReceiver(mBatteryInfoReceiver, new IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED));


        // Register for screen updates
        registerReceiver(mScreenInfoReceiver , new IntentFilter(
                    Intent.ACTION_SCREEN_OFF));

        registerReceiver(mScreenInfoReceiver , new IntentFilter(
                    Intent.ACTION_SCREEN_ON));

        // Register for WiFi state changes
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(
                WifiManager.NETWORK_STATE_CHANGED_ACTION);
        netIntentFilter.addAction(
                WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mNetInfoReceiver, netIntentFilter);


        // Register for call intents
        IntentFilter callIntentFilter = new IntentFilter();
        callIntentFilter.addAction(
                TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mCallInfoReceiver, callIntentFilter);
        
        mStateListener = new PhoneStateReceiver(mDbAdaptor, mConManager);
        mTelManager.listen(mStateListener,
        		PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
        		PhoneStateListener.LISTEN_CALL_STATE |
        		PhoneStateListener.LISTEN_CELL_LOCATION | 
        		PhoneStateListener.LISTEN_DATA_ACTIVITY |
        		PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
        		PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
        		PhoneStateListener.LISTEN_SERVICE_STATE |
        		PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
                
        
        /* This feature is too expensive and is not used any more.
        // Register for location information updates
        mLocManager = (LocationManager) getSystemService(
                LOCATION_SERVICE);
        mLocManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 
                MIN_GPS_TIME, MIN_GPS_DIST, 
                mLocationListener);
        mLocManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 
                MIN_GPS_TIME, MIN_GPS_DIST, 
                mLocationListener);
        */


        Intent alarmIntent = new Intent(SystemSens.this, 
                SystemSensAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(
                SystemSens.this, 0, alarmIntent, 0);
        long firstTime = SystemClock.elapsedRealtime() +
            POLLING_INTERVAL;

        AlarmManager am = (AlarmManager) getSystemService(
                    ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, POLLING_INTERVAL, sender);


        mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWL = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, 
                "SystemSenseUpload");
        mWL.setReferenceCounted(false);

        
        
        // Log a message indicating starting SystemSens
        JSONObject sysJson = new JSONObject();
        try
        {
            sysJson.put("state", "started");
            sysJson.put("release", Build.VERSION.RELEASE);
            sysJson.put("sdk_int", Build.VERSION.SDK_INT);
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }

        mDbAdaptor.createEntry( sysJson, SYSTEMSENS_TYPE);

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //showNotification();
        mVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); 




    }

    /** 
      * Clean up because we are going down.
      */
    @Override
    public void onDestroy() 
    {


        // Log a message indicating killing SystemSens
        JSONObject sysJson = new JSONObject();
        try
        {
            sysJson.put("state", "killed");
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }

        mDbAdaptor.createEntry( sysJson, SYSTEMSENS_TYPE);

        // Clear the message handler's pending messages
        mHandler.removeMessages(EVENTLOG_MSG);
        mHandler.removeMessages(NETLOG_MSG);
        mHandler.removeMessages(PROC_MSG);

        // Unregister event updates
        unregisterReceiver(mBatteryInfoReceiver);
        
        if (mIsScanning)
        {
        	unregisterReceiver(mWifiScanReceiver);
        	mIsScanning = false;
        }
        unregisterReceiver(mCallInfoReceiver);
        unregisterReceiver(mNetInfoReceiver);
        unregisterReceiver(mScreenInfoReceiver);
        
        // Unregister location updates
        //mLocManager.removeUpdates(mLocationListener);
        
        // Stop further WiFi scanning
        stopWifiScan();

        // Close the database adaptor
        /* Obsolete. DB is not kept open continously
        mDbAdaptor.close();
        */

        mNM.cancel(NOTIFICATION_ID);

        // Done!
        Log.i(TAG, "Killed");
    }

    /**
     * Class for clients to access.  
     */
    public class LocalBinder extends Binder 
    {
        SystemSens getService() 
        {
            return SystemSens.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) 
    {
         return mLocalBinder;
    }

    /** This is the object that receives interactions from clients. */
    private final IBinder mLocalBinder = new LocalBinder();

    /**
     * Broadcast receiver for WiFi scan results.
     * An object of this class has been passed to the system through 
     * registerRceiver.
     *
     */
    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                List<ScanResult> results = mWifi.getScanResults();

                HashMap<String, Integer> scanRes = 
                    new HashMap<String, Integer>();

                for (ScanResult result : results)
                {
                    scanRes.put(result.BSSID, result.level);
                }

                mDbAdaptor.createEntry( new JSONObject(scanRes), 
                            WIFISCAN_TYPE);
            }
        }
    };


    /**
     * Broadcast receiver for telephony updates.
     * An object of this class has been passed to the system through 
     * registerReceiver. 
     *
     */
    private BroadcastReceiver mCallInfoReceiver = new
        BroadcastReceiver()
    {
        /**
         * Method called whenever the  intent is received.
         * Logs the duration of the last outgoing or incoming call.
         */
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            int callState = mTelManager.getCallState(); 
            String state = " ";


            if (callState == TelephonyManager.CALL_STATE_OFFHOOK)
            {
                state = "started";
            }
            else if (callState == TelephonyManager.CALL_STATE_RINGING)
            {
                state = "ringing";
            }
            else if (callState == TelephonyManager.CALL_STATE_IDLE)
            {
                state = "ended";
            }


            JSONObject callJson = new JSONObject();
           

            try
            {
                callJson.put("state", state);
            }
            catch (JSONException e)
            {
                Log.e(TAG, "Exception", e);
            }

            mDbAdaptor.createEntry(  callJson, CALL_TYPE);
        }
    };

    /**
     * Broadcast receiver for WiFi  updates.
     * An object of this class has been passed to the system through 
     * registerReceiver. 
     *
     */
    private BroadcastReceiver mNetInfoReceiver = new
        BroadcastReceiver()
    {
        /**
         * Method called whenever the intent is received.
         * Gets the following information regarding the network
         * connectivity:
         *      Network state change
         * 
         */
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            NetworkInfo netInfo;
            String state = " ";

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) 
            { 
                netInfo = (NetworkInfo) intent.getParcelableExtra
                    (WifiManager.EXTRA_NETWORK_INFO);
                state = netInfo.getDetailedState().toString();

                JSONObject netJson = new JSONObject();
            }
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) 
            {
                int wifiState = (int) intent.getIntExtra
                    (WifiManager.EXTRA_WIFI_STATE, 0);

                switch (wifiState)
                {
                    case WifiManager.WIFI_STATE_DISABLED: 
                        state = "DISABLED"; 
                        stopWifiScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        state = "DISABLING";
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        state = "ENABLED";
                        startWifiScan();
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        state = "ENABLING";
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                    default:
                        state = "UNKNOWN";
                }
            }

            JSONObject netJson = new JSONObject();

            try
            {
                netJson.put("state", state);
            }
            catch (JSONException e)
            {
                Log.e(TAG, "Exception", e);
            }


            mDbAdaptor.createEntry(  netJson, NET_TYPE);
        }
    };

    private void startWifiScan()
    {
        //Register to receive WiFi scans
        registerReceiver(mWifiScanReceiver, new IntentFilter( 
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));


        mWifi.startScan();
        
        mIsScanning = true;
         
        Message wifimsg = mHandler.obtainMessage(WIFISCAN_MSG);
        long nextTime = SystemClock.uptimeMillis() 
            + WIFISCAN_INTERVAL;
        mHandler.sendMessageAtTime(wifimsg, nextTime);


    }

    private void stopWifiScan()
    {
        mHandler.removeMessages(WIFISCAN_MSG);
        if (mIsScanning)
        {
        	unregisterReceiver(mWifiScanReceiver);
        	mIsScanning = false;
        }
    }



    /**
     * Broadcast receiver for screen updates.
     * An object of this class has been passed to the system through 
     * registerReceiver. 
     *
     */
    private BroadcastReceiver mScreenInfoReceiver = new
        BroadcastReceiver()
    {
        /**
         * Method called whenever the intent is received.
         * Gets the following information regarding the screen:
         * o ON
         * o OFF
         *
         * When the screen turns on polling sensors are logged at a 
         * higher frequency. 
         * 
         */
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            String status = "";
            if (action.equals(Intent.ACTION_SCREEN_OFF)) 
            { 
                status = "OFF";


                // TODO: Set the polling interval to the
                // default values

            }
            else if (action.equals(Intent.ACTION_SCREEN_ON)) 
            { 
                status = "ON";

                // TODO: Set the polling sensor intervals to 30 seconds

            }

            JSONObject screenJson = new JSONObject();

            try
            {
                screenJson.put("status", status);
            }
            catch (JSONException e)
            {
                Log.e(TAG, "Exception", e);
            }

            Log.i(TAG, "Screen " + screenJson.toString());


            mDbAdaptor.createEntry( screenJson, SCREEN_TYPE);
        }
    };

   
    
    /**
     * Broadcast receiver for Battery information updates.
     * An object of this class has been passed to the system through 
     * registerReceiver. 
     *
     */
    private BroadcastReceiver mBatteryInfoReceiver = new
        BroadcastReceiver()
    {
        /**
         * Method called whenever the intent is received.
         * Gets the following information regarding the battery:
         *      Battery Status
         *      Battery voltage
         *      Battery level
         *      Battery health
         *      Battery temperature
         * 
         */
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            JSONObject batteryJson;
            
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) 
            {
                batteryJson = new JSONObject();

                // Get battery level
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int temp  = intent.getIntExtra("temperature", 0);

                try
                {
                   batteryJson.put(BATTERY_LEVEL, (level * 100 / scale));
                }
                catch (JSONException e)
                {
                    Log.e(TAG, "Exception", e);
                }

                // Get Battery status
                int plugType = intent.getIntExtra("plugged", 0);

                if (plugType > 0)
                {

                    mIsPlugged = true;
                    Status.setPlug(true);
                    Log.i(TAG, "Phone is plugged.");
                    Log.i(TAG, "Starting upload.");
                	upload();
                    //dump();

                }
                else
                {
                    // Launch the user interface.
                    if (mIsPlugged)
                    {
                        //mVib.vibrate(500);
                        mIsPlugged = false;
                        Status.setPlug(false);
                        /*
                        Intent uiActivityIntent = new Intent(
                                SystemSens.this,
                                SystemSensActivity.class);
                        uiActivityIntent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        SystemSens.this.startActivity(uiActivityIntent);
                        */

                    }
                }
                    
                int status = intent.getIntExtra("status",
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                String statusString = ""; 


                if (status == BatteryManager.BATTERY_STATUS_CHARGING) 
                {

                    mIsPlugged = true;
                    Status.setPlug(true);

                    statusString = "Charging";
                        
                    if (plugType > 0) 
                    {
                        if (plugType == BatteryManager.BATTERY_PLUGGED_AC) 
                            statusString = statusString + " (AC)";
                        else
                            statusString = statusString + " (USB)";
                    }
                } 
                else if (status == 
                        BatteryManager.BATTERY_STATUS_DISCHARGING) 
                {
                    statusString = "Discharging";
                    mIsPlugged = false;
                    Status.setPlug(false);
                } 
                else if (status == 
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING) 
                {
                    statusString = "Not charging";
                } 
                else if (status == BatteryManager.BATTERY_STATUS_FULL) 
                {
                    statusString = "Full";
                    mIsPlugged = true;
                } else 
                {
                    statusString = "Unknown";
                }

                try
                {
                   batteryJson.put("status", statusString);
                }
                catch (JSONException e)
                {
                    Log.e(TAG, "Exception", e);
                }
               
                
                
                // Get Battery health status
                int health = intent.getIntExtra("health",
                        BatteryManager.BATTERY_HEALTH_UNKNOWN);
                String healthString = " ";
                if (health == BatteryManager.BATTERY_HEALTH_GOOD) 
                {
                    healthString = "Good";
                } 
                else if (health == 
                        BatteryManager.BATTERY_HEALTH_OVERHEAT) 
                {
                    healthString = "Overheat";
                } 
                else if (health == BatteryManager.BATTERY_HEALTH_DEAD) 
                {
                    healthString = "Dead";
                } 
                else if (health ==
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE)
                {
                    healthString = "Over voltage";
                } 
                else if (health ==
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE)
                {
                    healthString = "Unspecified failure";
                } 
                else 
                {
                    healthString = "Health Uknown";
                }

                try
                {
                   batteryJson.put("health", healthString);
                }
                catch (JSONException e)
                {
                    Log.e(TAG, "Exception", e);
                }
 

                // Get battery temperature
                try
                {
                   batteryJson.put(BATTERY_TEMP, tenthsToFixedString(
                             temp));
                }
                catch (JSONException e)
                {
                    Log.e(TAG, "Exception", e);
                }
 
                // Get battery voltage
                try
                {
                   batteryJson.put("voltage", intent.getIntExtra( 
                               "voltage", 0));
                }
                catch (JSONException e)
                {
                    Log.e(TAG, "Exception", e);
                }


                mDbAdaptor.createEntry( batteryJson, BATTERY_TYPE);
                Status.setBattery(level, temp/10.0);
                 
            }
        }
    };
    
    LocationListener mLocationListener = new LocationListener()
    {
    	JSONObject gpsStateObject;
    	
    	public void onLocationChanged (Location location)
    	{
    		//
    	}
    	    	
    	public void onStatusChanged(String provider, int status, Bundle extra)
    	{
    		String statusStr, extraStr = "";
    		
    		
    		switch (status)
    		{
    		case LocationProvider.OUT_OF_SERVICE: 
                statusStr = "OutOfService"; break;
    		case LocationProvider.AVAILABLE: 
                statusStr = "Available"; break;
    		case LocationProvider.TEMPORARILY_UNAVAILABLE: 
                statusStr = "TemporarilyUnavailable"; break;
    		default: statusStr = "Uknown";
    		}
    		
    		for (String key : extra.keySet())
    			extraStr +=  extra.getInt(key);
    		
    		gpsStateObject = new JSONObject();
    		try
    		{
    			gpsStateObject.put("provider", provider);
    			gpsStateObject.put("status", statusStr);
    			gpsStateObject.put("extra", extraStr);
    		}
    		catch (JSONException je)
    		{
    			Log.e(TAG, "JSON Exception", je);
    		}

            mDbAdaptor.createEntry( gpsStateObject, GPSSTATE_TYPE);

    		
    	}

		@Override
		public void onProviderDisabled(String provider) {

    		gpsStateObject = new JSONObject();
    		try
    		{
    			gpsStateObject.put("provider", provider);
    			gpsStateObject.put("event", "disabled");
    		}
    		catch (JSONException je)
    		{
    			Log.e(TAG, "JSON Exception", je);
    		}

            mDbAdaptor.createEntry( gpsStateObject, GPSSTATE_TYPE);
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			
    		gpsStateObject = new JSONObject();
    		try
    		{
    			gpsStateObject.put("provider", provider);
    			gpsStateObject.put("event", "enabled");
    		}
    		catch (JSONException je)
    		{
    			Log.e(TAG, "JSON Exception", je);
    		}

            mDbAdaptor.createEntry(  gpsStateObject, GPSSTATE_TYPE);

			
		}
    };
    
    
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if (msg.what == UPLOAD_START_MSG)
            {
                mIsUploading = true;
            }

            if (msg.what == UPLOAD_END_MSG)
            {
                mIsUploading = false;
            }

            if (msg.what == WIFISCAN_MSG)
            {
                mWifi.startScan();

                msg = obtainMessage(WIFISCAN_MSG);
                long nextTime = SystemClock.uptimeMillis() 
                    + DEFAULT_WIFISCAN_INTERVAL;
                mHandler.sendMessageAtTime(msg, nextTime);

            }

        }

    };

 
   
    /** Format a number of tenths-units as a decimal string without using a
     *  conversion to float.  E.g. 347 -> "34.7"
     *  
     *  @param		intVal
     *  @return		String representing the decimal
     */
    private final String tenthsToFixedString(int intVal) 
    {
        int tens = intVal / 10;
        return new String("" + tens + "." + (intVal - 10*tens));
    }

    /**
     * Spawns a worker thread to "try" to upload the contents of the
     * database.
     * Before starting the thread, checks if a worker thread is
     * already trying to upload. If so, returns. Otherwise a new
     * thread is spawned and tasked with the upload job.
     * 
     */
    private void upload()
    {
        if (!mIsUploading)
        {
            Thread uploaderThread = new Thread()
            {
                public void run()
                {
                    mWL.acquire();
                    // Send an immediate message to the main thread
                    // to inform that a worker thread is running.
                    mHandler.sendMessageAtTime( mHandler.obtainMessage(
                                UPLOAD_START_MSG), 
                            SystemClock.uptimeMillis());

                    
                    Log.i(TAG, "Worker thread started upload task");
                    mUploader.tryUpload();

                    // Send an immediate message to the main thread to
                    // inform that the worker thread is finished.
                    mHandler.sendMessageAtTime( mHandler.obtainMessage(
                                UPLOAD_END_MSG), 
                            SystemClock.uptimeMillis());
                    mWL.release();
                }
            };

            uploaderThread.start();

        }
        else
        {
            Log.i(TAG, "Upload in progress ...");
        }
    }

    /**
     * Spawns a worker thread to "try" to write the contents of the
     * database in a flat file.
     * Before starting the thread, checks if a worker thread is
     * already trying to dump. If so, returns. Otherwise a new
     * thread is spawned and tasked with the dump job.
     *
     * This should only be used for debugging. It should NOT be used
     * along with the upload method. Either dump() or upload() should
     * be called.
     * 
     */
    /*
    private void dump()
    {
        if (!mIsUploading)
        {
            Thread uploaderThread = new Thread()
            {
                public void run()
                {
                    // Send an immediate message to the main thread
                    // to inform that a worker thread is running.
                    mHandler.sendMessageAtTime( mHandler.obtainMessage(
                                UPLOAD_START_MSG), 
                            SystemClock.uptimeMillis());

                    
                    Log.i(TAG, "Worker thread started dump task");
                    mDumper.tryDump();

                    // Send an immediate message to the main thread to
                    // inform that the worker thread is finished.
                    mHandler.sendMessageAtTime( mHandler.obtainMessage(
                                UPLOAD_END_MSG), 
                            SystemClock.uptimeMillis());
                }
            };

            uploaderThread.start();

        }
        else
        {
            Log.i(TAG, "Dump in progress ...");
        }
    }
    */


    public static boolean isPlugged()
    {
        return mIsPlugged;
    }


    /*
    private void showNotification()
    {
        CharSequence text = "SystemSens Service";

        Notification notification = new Notification(R.drawable.ss, 
                text, System.currentTimeMillis());

        notification.flags |= Notification.FLAG_NO_CLEAR;

        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, SystemSensActivity.class), 0);

        notification.setLatestEventInfo(this, 
                getText(R.string.app_name), text, contentIntent);

        mNM.notify("SystemSens", NOTIFICATION_ID, notification);
    }
    */





    private void pollingSensors()
    {
        if (mIsPlugged && (!mIsUploading))
            upload();


        Log.i(TAG, "Logging sensors");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO )
        {

            // Get network information 
            mDbAdaptor.createEntry( mNetLogger.getAppNetUsage(), 
                        NETLOG_TYPE);
            
            mDbAdaptor.createEntry(  mNetLogger.getIfNetUsage(), 
                    NETIFLOG_TYPE);



            // Get send and receive information 
            mEventLogger.update();
            
            mDbAdaptor.createEntry( mEventLogger.getActivityEvents(), 
                    ACTIVITYLOG_TYPE);

            mDbAdaptor.createEntry( mEventLogger.getServiceEvents(), 
                        SERVICELOG_TYPE);

            mDbAdaptor.createEntry( mEventLogger.getCpuEvents(), 
                        CPUSTAT_TYPE);

            mDbAdaptor.createEntry( mEventLogger.getMemEvents(), 
                        MEMSTAT_TYPE);
        }

        // Get /proc information
        mDbAdaptor.createEntry( mProc.getNetDev(), 
                    NETDEV_TYPE);
       
        mDbAdaptor.createEntry( mProc.getMemInfo(),
                MEMINFO_TYPE);
        
        mDbAdaptor.createEntry( mProc.getCpuLoad(),
                CPUSTAT_TYPE);


        mDbAdaptor.flushDb();

        // Release the wakelock
        SystemSensWakeLock.releaseCpuLock();



    }
}

