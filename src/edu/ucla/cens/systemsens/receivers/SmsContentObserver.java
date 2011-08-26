package edu.ucla.cens.systemsens.receivers;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.systemlog.Log;
import edu.ucla.cens.systemsens.util.HashPrinter;
import edu.ucla.cens.systemsens.util.SystemSensDbAdaptor;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class SmsContentObserver extends ContentObserver
{
	private static final String TAG = "sms context observer";
	/** MessageDigest object to compute MD5 hash */
    private MessageDigest mDigest;

	public SmsContentObserver()
	{
		super(null);

	}

	public SmsContentObserver(Context context, 
            SystemSensDbAdaptor mDbAdaptor)
	{
		super(null);
		this.context = context;
		this.mDbAdaptor = mDbAdaptor;
        try
        {
     	   mDigest = java.security.MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException nae)
        {
     	   Log.e(TAG, "Exception", nae);
        }

	}

	private static final String SMS = "sms";

	private SystemSensDbAdaptor mDbAdaptor;
	Context context;
	Long lastSmsTime = 0l;

	@Override
	public void onChange(boolean selfChange)
	{
		super.onChange(selfChange);
		Uri uriSMSURI = Uri.parse("content://sms/");
		Cursor cur = context.getContentResolver().query(
                uriSMSURI, null, null, null, null);
		cur.moveToNext();
		String protocol = cur.getString(cur.getColumnIndex("protocol"));
		Long timestamp = cur.getLong(cur.getColumnIndex("date"));

		// check for repeats and stale data and sms sent
		if (lastSmsTime < timestamp && timestamp > 
                System.currentTimeMillis() - 1000 * 10 && protocol == null) 
		{
			lastSmsTime = timestamp;
			String textBody = cur.getString(cur.getColumnIndex("body"));
			int msgLen = textBody.length();
			String toAddr = cur.getString(cur.getColumnIndex("address"));
			byte[] byteKey = mDigest.digest(toAddr.getBytes());
			String hashAddr = HashPrinter.hashString(byteKey);
			JSONObject json = new JSONObject();

			try
			{

				json.put("ToAddress", hashAddr);
				json.put("MessageLength", msgLen);
				json.put("MessageTimestamp", timestamp);
			}
			catch (JSONException je)
			{
                Log.e(TAG, "Could not write to JSON object", je);
			}
			mDbAdaptor.createEntry(json, SMS);
		}
	}
}
