package edu.ucla.cens.systemsens.receivers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.telephony.SmsMessage;
import android.os.Bundle;
import android.util.Log;


import edu.ucla.cens.systemsens.SystemSens;
import edu.ucla.cens.systemsens.util.HashPrinter;
import edu.ucla.cens.systemsens.util.SystemSensDbAdaptor;


public class SmsReceiver extends BroadcastReceiver
{
	private static final String TAG = "SystemSensSmsReceiver";
	
	private static final String SMS = "sms";
	
	private SystemSensDbAdaptor mDbAdaptor;
	private MessageDigest mDigest;

    public SmsReceiver(SystemSensDbAdaptor dbAdaptor)
    {
        mDbAdaptor = dbAdaptor;
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
    public void onReceive(Context context, Intent intent)
    {
        Bundle bundle = intent.getExtras();


        JSONObject json = new JSONObject();

        String fromAddr;
        int msgLen;
        long msgTime;

        Object messages[] = (Object[]) bundle.get("pdus");
        SmsMessage smsMessage;
        for (int n = 0; n < messages.length; n++) 
        {
            smsMessage = SmsMessage.createFromPdu((byte[]) messages[n]); 

            fromAddr = smsMessage.getOriginatingAddress().substring(1);
            msgLen = smsMessage.getMessageBody().length();
            msgTime = smsMessage.getTimestampMillis();
            byte[] byteKey = mDigest.digest(fromAddr.getBytes());
			String hashAddr = HashPrinter.hashString(byteKey);
            Log.i(TAG, "From (hashed): " + hashAddr);
            Log.i(TAG, "Message Length: " +
                    smsMessage.getMessageBody().length());
            Log.i(TAG, "Timestamp: " +
                    smsMessage.getTimestampMillis());
            
            try
            {

                json.put("FromAddress", hashAddr);
                json.put("MessageLength", msgLen);
                json.put("MessageTimestamp", msgTime);
            }
            catch (JSONException je)
            {

            }
            
            mDbAdaptor.createEntry(json, SMS);

        }



    }


}
