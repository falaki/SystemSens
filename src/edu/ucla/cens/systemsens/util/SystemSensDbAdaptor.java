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


package edu.ucla.cens.systemsens.util;


import org.json.JSONObject;
import org.json.JSONException;

import java.util.Calendar;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.PowerManager;
import android.util.Log;

import edu.ucla.cens.systemsens.SystemSens;

/**
 * Simple  database access helper class. 
 * Interfaces with the SQLite database to store system information.
 * Written based on sample code provided by Google.
 *
 * @author Hossein Falaki
 */
public class SystemSensDbAdaptor 
{


    private static final String VER = SystemSens.VER;
    private static final String IMEI = SystemSens.IMEI;

    public static final String KEY_DATARECORD = "datarecord";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TYPE = "recordtype";
    public static final String KEY_TIME = "recordtime";


    private static final String TAG = "SystemSensDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /** Database creation sql statement */
    private static final String DATABASE_CREATE =
            "create table systemsens (_id integer primary key "
           + "autoincrement, recordtime text not null, " 
           + "recordtype text not null, datarecord text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "systemsens";
    private static final int DATABASE_VERSION = 4;



    private HashSet<ContentValues> mBuffer;
    private HashSet<ContentValues> tempBuffer;

    private boolean mOpenLock = false;
    private boolean mFlushLock = false;


    private final Context mCtx;
    private final PowerManager.WakeLock mWL;

    private static class DatabaseHelper extends SQLiteOpenHelper 
    {

        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS systemsens");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx       the Context within which to work
     */
    public SystemSensDbAdaptor(Context ctx) 
    {
        this.mCtx = ctx;
        mBuffer = new HashSet<ContentValues>(); 

        PowerManager pm = (PowerManager)
            ctx.getSystemService(Context.POWER_SERVICE);

        mWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG);
        mWL.setReferenceCounted(false);

    }

    /**
     * Open the database.
     * If it cannot be opened, try to create a new instance of the
     * database. If it cannot be created, throw an exception to signal
     * the failure.
     * 
     * @return this         (self reference, allowing this to be
     *                      chained in an initialization call)
     * @throws SQLException if the database could be neither opened or
     *                      created
     */
    public SystemSensDbAdaptor open() throws SQLException 
    {
        if (!mFlushLock)
        {
            mDbHelper = new DatabaseHelper(mCtx);
            mDb = mDbHelper.getWritableDatabase();
        }
        mOpenLock = true;
        return this;
    }
    
    /**
      * Closes the database.
      */
    public void close() 
    {
        if (!mFlushLock)
            mDbHelper.close();
        mOpenLock = false;
    }


    /**
     * Create a new entry using the datarecord provided. 
     * If the entry is successfully created returns the new rowId for
     * that entry, otherwise returns a -1 to indicate failure.
     * 
     * @param datarecord        datarecord for the entry
     */
    public synchronized void createEntry(JSONObject data, String type) 
    {

        JSONObject dataRecord = new JSONObject();

        // First thing, get the current time
        Calendar cal = Calendar.getInstance();
        String timeStr = "" +
            cal.get(Calendar.YEAR) + "-" +
            (cal.get(Calendar.MONTH) + 1) + "-" +
            cal.get(Calendar.DAY_OF_MONTH) + " " +
            cal.get(Calendar.HOUR_OF_DAY) + ":" +
            cal.get(Calendar.MINUTE) + ":" +
            cal.get(Calendar.SECOND);



        try
        {
            dataRecord.put("date", timeStr);
            dataRecord.put("time_stamp", cal.getTimeInMillis());
            dataRecord.put("user", IMEI);
            dataRecord.put("type", type);
            dataRecord.put("ver", VER);
            dataRecord.put("data", data);
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Exception", e);
        }



        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TIME, timeStr);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_DATARECORD, dataRecord.toString());

        mBuffer.add(initialValues);

        //Log.i(TAG, "Creating data record" + dataRecord.toString());

        //return mDb.insert(DATABASE_TABLE, null, initialValues);
    }


    public synchronized void flushDb()
    {
        tempBuffer = mBuffer;
        mBuffer = new HashSet<ContentValues>(); 

        Thread flushThread = new Thread()
        {
            public void run()
            {
                mWL.acquire();

                if (!mOpenLock)
                {
                    try
                    {
                        mDbHelper = new DatabaseHelper(mCtx);
                        mDb = mDbHelper.getWritableDatabase();
                    }
                    catch (SQLException se)
                    {
                        Log.e(TAG, "Could not open DB helper", se);

                    }
                }
                mFlushLock = true;


                Log.i(TAG, "Flushing " 
                        + tempBuffer.size() + " records.");

                for (ContentValues value : tempBuffer)
                {
                    mDb.insert(DATABASE_TABLE, null, value);
                }


                if (!mOpenLock)
                    mDbHelper.close();

                mFlushLock = false;
                mWL.release();
            }
        };

        flushThread.start();

    }

    /**
     * Deletes the entry with the given rowId
     * 
     * @param rowId         id of datarecord to delete
     * @return              true if deleted, false otherwise
     */
    public synchronized boolean deleteEntry(long rowId) 
    {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID 
                + "=" + rowId, null) > 0;
    }


    /**
     * Deletes the entries in a range.
     * 
     * @param fromId         id of first datarecord to delete
     * @param toId           id of last datarecord to delete
     * @return              true if deleted, false otherwise
     */
    public synchronized boolean deleteRange(long fromId, long toId) 
    {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID 
                + " BETWEEN " 
                + fromId
                + " AND " 
                + toId, null) > 0;
    }


    /**
     * Returns a Cursor over the list of all datarecords in the database
     * 
     * @return              Cursor over all notes
     */
    public Cursor fetchAllEntries() 
    {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_TIME, KEY_TYPE, KEY_DATARECORD}, 
                null, null, null, null, null);
    }

    /**
     * Returns a Cursor positioned at the record that matches the
     * given rowId.
     * 
     * @param  rowId        id of note to retrieve
     * @return              Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchEntry(long rowId) throws SQLException 
    {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]
                {KEY_ROWID, KEY_TIME, KEY_TYPE, KEY_DATARECORD}, 
                KEY_ROWID + "=" + rowId,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


}
