package edu.ucla.cens.systemsens.receivers;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.systemlog.Log;
import edu.ucla.cens.systemsens.util.HashPrinter;
import edu.ucla.cens.systemsens.util.SystemSensDbAdaptor;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class CalendarContentObserver extends ContentObserver
{
	private static final String TAG = "calendar content observer";

	/** MessageDigest object to compute MD5 hash */
    private MessageDigest mDigest;

	public CalendarContentObserver()
	{
		super(null);

	}

	public CalendarContentObserver(
            Context context, SystemSensDbAdaptor mDbAdaptor)
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

        contentResolver = context.getContentResolver();
	}

	ContentResolver contentResolver;
	private static final String CALENDAR = "calendar";

	private SystemSensDbAdaptor mDbAdaptor;
	Context context;
	Long lastSmsTime = 0l;
	HashMap<String, String> events = new HashMap<String, String>();
	long lastTime = 0;
	@Override
	public void onChange(boolean selfChange)
	{
		super.onChange(selfChange);
		Uri uriSMSURI = Uri.parse(
                "content://com.android.calendar/calendars");
		Cursor cursor = context.getContentResolver().query(
                uriSMSURI, null, null, null, null);
		
		HashSet<String> calendarIds = new HashSet<String>();
		while (cursor.moveToNext())
		{

			final String _id = cursor.getString(0);
			calendarIds.add(_id);
		}
		JSONArray jsonArray = new JSONArray();
		int index = 0;
		// For each calendar, display all the events 
        // from the previous week to
		// the end of next week.
		if (System.currentTimeMillis() < lastTime + 3000)
			return;
		for (String id : calendarIds)
		{
			Uri.Builder builder = Uri.parse(
                    "content://com.android.calendar/instances/when").buildUpon();
			long now = new Date().getTime();
			ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
			ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);

			Cursor eventCursor = contentResolver.query(builder.build(), new String[] { "title", "begin", "end", "allDay" }, "Calendars._id=" + id, null, "startDay ASC, startMinute ASC");
			// For a full list of available columns see
			// http://tinyurl.com/yfbg76w
			
			
			while (eventCursor.moveToNext())
			{
				final String title = eventCursor.getString(0);
				final Long begin = eventCursor.getLong(1);
				final Long end = eventCursor.getLong(2);
				final Boolean allDay = !eventCursor.getString(3).equals("0");
				JSONObject json = new JSONObject();
				byte[] byteKey = mDigest.digest(title.getBytes());
				String hashTitle = HashPrinter.hashString(byteKey);
				try
				{
					json.put("Title", hashTitle);
					json.put("Start", begin);
					json.put("End", end);
					json.put("AllDay", allDay);
					jsonArray.put(json);
				}
				catch (JSONException je)
				{
					Log.e(TAG, "JSON exception happened");
				}
				
			}
			
		}
		
		JSONObject json = new JSONObject();
		try
		{
			json.put(System.currentTimeMillis() + "", jsonArray); // Make array and check for dupes.
//			Log.i(TAG, json.toString().length() + " " + json.toString());
			mDbAdaptor.createEntry(json, CALENDAR);
			lastTime = System.currentTimeMillis();
		}
		catch (JSONException je)
		{
			Log.e(TAG, "JSON exception happened");
		}
		
		
		
//		cur.moveToNext();
//		String protocol = cur.getString(cur.getColumnIndex("protocol"));
		 

//		Long timestamp = cur.getLong(cur.getColumnIndex("date"));
//		// check for repeats and stale data and sms sent
//		if (lastSmsTime < timestamp && timestamp > System.currentTimeMillis() - 1000 * 10 && protocol == null) 
//		{
//			lastSmsTime = timestamp;
////			Log.i(TAG, "SMS SEND");
//			String textBody = cur.getString(cur.getColumnIndex("body"));
//			int msgLen = textBody.length();
//			String toAddr = cur.getString(cur.getColumnIndex("address"));
//			byte[] byteKey = mDigest.digest(toAddr.getBytes());
//			String hashAddr = HashPrinter.hashString(byteKey);
////			Log.i(TAG, "SMS SEND ADDRESS= " + toAddr);
//			// Log.i(TAG, "SMS SEND BODY= " + textBody);
//			JSONObject json = new JSONObject();
//			try
//			{
//
//				json.put("ToAddress", hashAddr);
//				json.put("MessageLength", msgLen);
//				json.put("MessageTimestamp", timestamp);
//			}
//			catch (JSONException je)
//			{
//
//			}
//			Log.i(TAG, json.toString());
//			mDbAdaptor.createEntry(json, SMS);
//		}
//		else
//		{
////			Log.e(TAG, lastSmsTime + " >= " + timestamp + " or its <= " + (System.currentTimeMillis() - 10000) + " or protocol being null is " + (protocol == null));
//		}
		// if (protocol == null)
		// {
		// Log.i(TAG, "SMS SEND");
		// threadId = cur.getInt(cur.getColumnIndex("thread_id"));
		// int status = cur.getInt(cur.getColumnIndex("status"));
		// String textBody = cur.getString(cur.getColumnIndex("body"));
		// String textAddress = cur.getString(cur.getColumnIndex("address"));
		// Log.i(TAG, "SMS SEND ADDRESS= " + textAddress);
		// Log.i(TAG, "SMS SEND BODY= " + textBody);
		//
		// }
		// else
		// {
		// Log.i(TAG, "SMS RECEIVE");
		//
		// }
		// Log.i(TAG, "Good bye!");
		// Log.i(TAG, "Starting service!");
		// context.startService(new Intent(context, SmsHelperService.class));
		// Log.i(TAG, "Service started!");
	}
}
