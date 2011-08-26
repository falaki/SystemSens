/**
 * SystemSens
 *
 * Copyright (C) 2009 Hossein Falaki
 */
package edu.ucla.cens.systemsens.util;

import android.database.Cursor;
import android.database.SQLException;

import java.lang.ProcessBuilder;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;


//import android.util.Log;
import edu.ucla.cens.systemlog.Log;

import edu.ucla.cens.systemsens.SystemSens;



/**
 * This class implements mechanisms to upload data collected by
 * SystemSens to Sensorbase (or any other repository).
 * It is passed a pointer to a Database Adaptor object upon creation.
 * Each time the upload() method is called a new thread is spawned.
 * The new thread will read all the records in the
 * database and uploaded and then delete them.
 *
 * @author  Hossein Falaki
 */
public class Uploader
{
    /** Tag used for log messages */
    private static final String TAG = "SystemSensUploader";

    /** Database adaptor object */
    private SystemSensDbAdaptor mDbAdaptor;

    /** Maximum number of records that will be read and deleted at a
     * time*/
    private static final int MAX_UPLOAD_SIZE = 200;

    /** After this number of failiurs upload will abort */
    private static final int MAX_FAIL_COUNT = 50;


    /** Upload location of the SystemSens server */
    private static final String CUSTOM_URL 
        = "https://systemsens.cens.ucla.edu/service/viz/put/";


    private static final String IMEI = SystemSens.IMEI;


    private static final String mTableName = "systemsens";

    /**
     * Constructor - creates an uploader object with access to the
     * given database adaptor object. 
     *
     * @param   dbAdaptor       database adaptor object
     */
    public Uploader(SystemSensDbAdaptor dbAdaptor)
    {
        this.mDbAdaptor = dbAdaptor;
    }





    public void tryUpload()
    {
        //TODO: hold a wifi lock

        Log.i(TAG, "tryUpload started");
        Cursor  c = null;
        boolean postResult = false;
        boolean noError = true;

        try
        {
            mDbAdaptor.open();


            c = mDbAdaptor.fetchAllEntries();
            int dataIndex = c.getColumnIndex(
                    SystemSensDbAdaptor.KEY_DATARECORD);
            int idIndex = c.getColumnIndex(
                    SystemSensDbAdaptor.KEY_ROWID);
            int timeIndex = c.getColumnIndex(
                    SystemSensDbAdaptor.KEY_TIME);
            int typeIndex = c.getColumnIndex(
                    SystemSensDbAdaptor.KEY_TYPE);


            Integer id, pId;
            HashSet<Integer> keySet = new HashSet<Integer>();
            String newRecord, newType, newTime;
            String line;
            ArrayList<String> content;

            int failCount = 0;
            int dbSize =  c.getCount();
            int readCount = 0;



            c.moveToFirst();

            while ((dbSize > 0) && SystemSens.isPlugged() && noError)
            {
                
                Log.i(TAG, "Total DB size is: " + dbSize);
                int maxCount = (MAX_UPLOAD_SIZE > dbSize) 
                    ? dbSize : MAX_UPLOAD_SIZE;

                content = new ArrayList<String>();
                pId = -1;

                for (int i = 0; i < maxCount; i++)
                {

                    id = c.getInt(idIndex);
                    if ((pId != -1) && (id != (pId + 1)))
                    {
                        Log.i(TAG, "Cursor jumped.");
                        noError = false;
                        break;
                    }
                    newRecord = c.getString(dataIndex);

                    content.add(URLEncoder.encode(newRecord));
                    keySet.add(id);
                    readCount++;
                    pId = id;

                    c.moveToNext();
                    
                }
                
                dbSize -= readCount;


                do
                {
                    postResult = doPost("data=" 
                            + content.toString(), CUSTOM_URL);
                    if (postResult)
                    {
                        failCount = 0;
                        long fromId = Collections.min(keySet);
                        long toId = Collections.max(keySet);
                        Log.i(TAG, "Deleting [" 
                                + fromId + ", " + toId + "]");

                        if( !mDbAdaptor.deleteRange(fromId, toId) )
                        {
                            Log.e(TAG, "Error deleting rows");
                        }

                    }
                    else
                    {
                        Log.e(TAG, "Post failed");
                        failCount++;
                    }
                    keySet.clear();
                }
                while ( (!postResult) && (failCount < MAX_FAIL_COUNT));

                if (failCount > MAX_FAIL_COUNT)
                {
                    Log.e(TAG, "Too many post failiurs. "
                            + "Will try at another time");
                    c.close();
                    mDbAdaptor.close();
                    return;
                }

            }
            
            c.close();
            
            // Tickle the DB
            mDbAdaptor.tickle();
            mDbAdaptor.close();
            
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception", e);
            Log.i(TAG, "Will resume upload later");

            if (c != null)
            {
                c.close();
            }
            mDbAdaptor.close();
        }



    }


    private boolean doPost(String content, String dest) 
    {
        OutputStream out;
        byte[] buff;
        int respCode;
        String respMsg = "";
        HttpURLConnection con;
        URL url;
        try
        {
            url = new URL(dest);
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG, "Exception", e);
            return false;
        }


        try
        {
            con = (HttpURLConnection) url.openConnection();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Exception", e);
            return false;
        }


        try
        {
            con.setRequestMethod("POST");
        }
        catch (java.net.ProtocolException e)
        {
            Log.e(TAG, "Exception", e);
            return false;
        }
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-type", 
                "application/x-www-form-urlencoded");

        try
        {
            con.connect();
            out = con.getOutputStream();
            buff = content.getBytes("UTF8");
            out.write(buff);
            out.flush();


            respMsg = con.getResponseMessage();
            respCode = con.getResponseCode();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Exception", e);
            con.disconnect();
            return false;
        }



        if (respCode == HttpURLConnection.HTTP_OK)
        {
            con.disconnect();
            return true;
        }
        else
        {
            Log.e(TAG, "post failed with error: " 
                    + respMsg);
            con.disconnect();
            return false;
        }
    }

}
