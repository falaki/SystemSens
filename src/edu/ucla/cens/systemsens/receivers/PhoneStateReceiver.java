package edu.ucla.cens.systemsens.receivers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;


import edu.ucla.cens.systemsens.SystemSens;
import edu.ucla.cens.systemsens.util.HashPrinter;
import edu.ucla.cens.systemsens.util.SystemSensDbAdaptor;


public class PhoneStateReceiver extends PhoneStateListener
{
	private static final String TAG = "SystemSensLite-Receiver";
	
	private static final String CALLFORWARDING = "callforwarding";
	private static final String CELLLOCATION = "celllocation";
	private static final String CALLSTATE = "callstate";
	private static final String DATAACTIVITY = "dataactivity";
	private static final String DATACONNECTION = "dataconnection";
	private static final String MESSAGEWAITING = "message";
	private static final String SIGNALSTRENGTH = "signal";
	private static final String SERVICESTATE = "servicestate";
	
	private ConnectivityManager mConManager;
	private SystemSensDbAdaptor mDbAdaptor;
	private Context context;
	CellLocation mLastCellLoc = null;
	private MessageDigest mDigest;
	public PhoneStateReceiver(SystemSensDbAdaptor dbAdaptor, 
            ConnectivityManager conManager, Context context)
	{
		mDbAdaptor = dbAdaptor;
		mConManager = conManager;
		this.context=context;
		try
        {
     	   mDigest = java.security.MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException nae)
        {
     	   Log.e(TAG, "Exception", nae);
        }
	}
	
	@Override
	public void onCallForwardingIndicatorChanged(boolean cfi)
	{
		

		JSONObject json = new JSONObject();
         

        try
        {
        	if (cfi)
        		json.put("state", "true");
        	else
        		json.put("state", "false");
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }
		mDbAdaptor.createEntry(json, CALLFORWARDING);
		
		//Log.i(TAG, "Call forwarding state" + cfi);
		
	}
	String lastNumber = "unknown";
	long lastRecordedCallTime = 0;
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		String stateStr;
		
		switch (state)
		{
		case TelephonyManager.CALL_STATE_IDLE:
			stateStr = "idle"; break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			stateStr = "offhook"; break;
		case TelephonyManager.CALL_STATE_RINGING:
			stateStr = "ringing"; break;
		default:
			stateStr = "unknown";
		}
		
		JSONObject json = new JSONObject();
        

        if (SystemSens.ADDL_SENSORS)
        {
            try
            {
                json.put("state", stateStr);
                if (state == TelephonyManager.CALL_STATE_RINGING)
                {
                    json.put("number", hashNumber(incomingNumber));
                    lastRecordedCallTime = System.currentTimeMillis();
                    lastNumber = incomingNumber;
                }
                else if (state == TelephonyManager.CALL_STATE_OFFHOOK)
                {
                    json.put("number", 
                            hashNumber(getLastCallLogEntry(context)));

                }
                else
                {
                	lastNumber = "unknown";
                	lastRecordedCallTime = System.currentTimeMillis();
                }
            }
            catch (JSONException e)
            {
                Log.e(TAG, "Exception", e);
            }
        }
		mDbAdaptor.createEntry(json, CALLSTATE);
		Log.i(TAG, json.toString());
	}
	
//	BroadcastReceiver onOutgoingCall
	
	private String hashNumber(String number)
	{
		if (number == null || number.startsWith("-"))
			return "unknown";
//		else if (true) return number;
		byte[] byteKey = mDigest.digest(number.getBytes());
		return HashPrinter.hashString(byteKey);
	}
	
	private String getLastCallLogEntry( Context context ) { 
//		try
//		{
//			Thread.sleep(20000);
//		}
//		catch (InterruptedException e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        String[] projection = new String[] { 
                BaseColumns._ID, 
                CallLog.Calls.NUMBER, 
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
              }; 
              ContentResolver resolver = context.getContentResolver(); 
              Cursor cur = resolver.query(  
                            CallLog.Calls.CONTENT_URI, 
                            projection,  
                            null, 
                            null, 
                            CallLog.Calls.DEFAULT_SORT_ORDER ); 
              int numberColumn = cur.getColumnIndex( CallLog.Calls.NUMBER );  
              int typeColumn = cur.getColumnIndex( CallLog.Calls.TYPE ); 
              int dateColumn = cur.getColumnIndex(CallLog.Calls.DATE);
              if( !cur.moveToNext() ) { 
                cur.close(); 
                return ""; 
              } 
              
              String number = cur.getString( numberColumn ); 
              long date = cur.getLong(dateColumn);
//              Log.e(TAG, "The date is " + (System.currentTimeMillis()-date)/1000 + " seconds old!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              if (date < lastRecordedCallTime)
            	  return lastNumber;
              lastRecordedCallTime = date;
              lastNumber = number;
              String type = cur.getString( typeColumn ); 
              String dir = null; 
              try { 
                int dircode = Integer.parseInt( type ); 
                switch( dircode ) { 
                  case CallLog.Calls.OUTGOING_TYPE: 
                        dir = "OUTGOING";
                        break; 
 
                  case CallLog.Calls.INCOMING_TYPE: 
                        dir = "INCOMING"; 
                        break; 
 
                  case CallLog.Calls.MISSED_TYPE: 
                        dir = "MISSED"; 
                        break; 
                } 
              } catch( NumberFormatException ex ) {} 
              if( dir == null ) 
                    dir = "Unknown, code: "+type; 
              Log.i(TAG, "Ahoy! " + dir);

              cur.close(); 
              return number; 
 
    }
	
	@Override
	public void onCellLocationChanged(CellLocation location)
	{
		if ( mLastCellLoc != null)
		{
			if (!mLastCellLoc.equals(location))
			{
				mLastCellLoc = location;
				
				JSONObject json = new JSONObject();
		        
		        try
		        {
		        	json.put("location", location.toString());
		        }
		        catch (JSONException e)
		        {
		            Log.e(TAG, "Exception", e);
		        }
				mDbAdaptor.createEntry(json, CELLLOCATION);				
				
				//Log.i(TAG, "Cell Location changed to" + location.toString());
			}
		}
		else
		{
			mLastCellLoc = location;
		}
	
	}
	
	
	/*
	@Override
	public void onDataActivity(int direction)
	{
		String dirStr;
		
		switch (direction)
		{
		case TelephonyManager.DATA_ACTIVITY_NONE:
			dirStr = "none"; break;
		case TelephonyManager.DATA_ACTIVITY_IN:
			dirStr = "in"; break;
		case TelephonyManager.DATA_ACTIVITY_OUT:
			dirStr = "out"; break;    			
		case TelephonyManager.DATA_ACTIVITY_INOUT:
			dirStr = "inout"; break;    			
		case TelephonyManager.DATA_ACTIVITY_DORMANT:
			dirStr = "dormant"; break;    			    			
		default:
			dirStr = "unknown"; break;
		}
		
		JSONObject json = new JSONObject();
        
        try
        {
        	json.put("direction", dirStr);
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }
		mDbAdaptor.createEntry(json, DATAACTIVITY);		
		    		
		Log.i(TAG, "onDataActivity direction: " + dirStr);
	}
	*/
	
	@Override
	public void onDataConnectionStateChanged(int state, int networkType)
	{
		String stateStr, netTypeStr;
		
		switch (state)
		{
		case TelephonyManager.DATA_DISCONNECTED:
			stateStr = "disconnected"; break;
		case TelephonyManager.DATA_CONNECTED:
			stateStr = "connected"; break;
		case TelephonyManager.DATA_CONNECTING:
			stateStr = "connecting"; break;			
		case TelephonyManager.DATA_SUSPENDED:
			stateStr = "suspended"; break;
		default: 
			stateStr = "unkown"; break;
		}


        NetworkInfo  netInfo =
            mConManager.getNetworkInfo(networkType);

        if (netInfo != null)
		    netTypeStr = netInfo.getTypeName();
        else
            netTypeStr = "Unknown";
		 
		JSONObject json = new JSONObject();
        

        try
        {
        	json.put("network", netTypeStr);
        	json.put("state", stateStr);
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }
		mDbAdaptor.createEntry(json, DATACONNECTION);
		
		//Log.i(TAG, "onDataConnectionStateChanged on network " + netTypeStr + " to " + stateStr);
	}
	
	
	@Override
	public void onMessageWaitingIndicatorChanged(boolean mwi)
	{
		JSONObject json = new JSONObject();        

        try
        {
        	if (mwi)
        		json.put("state", "true");
        	else
        		json.put("state", "false");
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }
        mDbAdaptor.createEntry(json, MESSAGEWAITING);
        
		//Log.i(TAG, "Message Waiting");
	}
	
	@Override
	public void onServiceStateChanged(ServiceState serviceState)
	{
		int state = serviceState.getState();
		String stateStr;
		
		switch (state)
		{
		case ServiceState.STATE_EMERGENCY_ONLY:
			stateStr = "emergency"; break;
		case ServiceState.STATE_IN_SERVICE:
			stateStr = "inservice"; break;
		case ServiceState.STATE_OUT_OF_SERVICE:
			stateStr = "outofservice"; break;
		case ServiceState.STATE_POWER_OFF:
			stateStr = "poweroff"; break;
		default:
			stateStr = "unknown"; break;

		}
		JSONObject json = new JSONObject();        

        try
        {
        	json.put("operator_alpha", serviceState.getOperatorAlphaLong());
        	json.put("operator_numeric", serviceState.getOperatorNumeric());
        	if (serviceState.getRoaming())
        		json.put("roaming", "true");
        	else
        		json.put("roaming", "false");
        	json.put("state", stateStr);
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }		
        mDbAdaptor.createEntry(json, SERVICESTATE);
		//Log.i(TAG, "Service state " + serviceState.toString());
	}
	
	
	@Override
	public void onSignalStrengthsChanged(SignalStrength signal)
	{
		
		JSONObject json = new JSONObject();        

        try
        {
        	if (signal.isGsm())
        	{
        		json.put("signal", signal.getGsmSignalStrength());
        		json.put("biterror", signal.getGsmBitErrorRate());
        	}
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }		
        mDbAdaptor.createEntry(json, SIGNALSTRENGTH);
		//Log.i(TAG, "onSignalStrengthChanged to " + signal.toString());
	}

	
	
};
