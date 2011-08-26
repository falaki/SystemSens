/**
 * PowerDbAdaptor for SystemSens
 *
 * Copyright (C) 2011 Hossein Falaki
 */
package edu.ucla.cens.systemsens.util;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.ucla.cens.systemsens.SystemSens;
import edu.ucla.cens.systemlog.Log;

/**
 * Simple  database access helper class. 
 * Interfaces with the SQLite database to store WiFi cache objects.
 *
 * @author Hossein Falaki
 */
public class PowerDbAdaptor 
{


    private static final String VER = SystemSens.VER;
    private static final String IMEI = SystemSens.IMEI;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_TIME = "settime";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_DEADLINE = "deadline";




    private static final String TAG = "PowerDbAdaptor";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /** Database creation sql statement */
    private static final String DATABASE_CREATE =
        "create table power (_id integer primary key "
        + "autoincrement, settime integer not null, "
        + "deadline integer not null, "
        + "level integer not null);";

    private static final String DATABASE_NAME = "powerdb";
    private static final String DATABASE_TABLE = "power";
    private static final int DATABASE_VERSION = 4;

    private final Context mCtx;

    private Calendar mSetTime;
    private int mDeadline;
    private int mLevel;
    private boolean mHasDeadline = false;


    private SimpleDateFormat mSDF;

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
            db.execSQL("DROP TABLE IF EXISTS power");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx       the Context within which to work
     */
    public PowerDbAdaptor(Context ctx) 
    {
        this.mCtx = ctx;
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
    public PowerDbAdaptor open() throws SQLException 
    {

        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();

        mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm", 
                Locale.US); 



        return this;
    }
    
    /**
      * Closes the database.
      */
    public void close() 
    {
        mDbHelper.close();
    }

    
    /**
     *
     */
    public void saveDeadline(Calendar setTime, int deadline, int level) 
    {
        this.open();

        long setTimeVal = setTime.getTimeInMillis();


        int dbCount = mDb.delete(DATABASE_TABLE, "1", null);
        Log.i(TAG, "Deleted database content: " + dbCount + " rows.");


        ContentValues initialValues;
        initialValues = new ContentValues();


        initialValues.put(KEY_TIME, setTimeVal);
        initialValues.put(KEY_DEADLINE, deadline);
        initialValues.put(KEY_LEVEL, level);

        mDb.insert(DATABASE_TABLE, null, initialValues);

        String resStr = "deadline " 
            + deadline
            + " submitted at " 
            + mSDF.format(setTime.getTime()) 
            + " (" + level + "%)";

        
        mHasDeadline = true;
        mSetTime = setTime;
        mLevel = level;
        mDeadline = deadline;


        Log.i(TAG, "Saved " + resStr);

        this.close();

    }

    /**
     * Deletes the entry with the given rowId
     * 
     * @param rowId         id of datarecord to delete
     * @return              true if deleted, false otherwise
     */
    public boolean deleteEntry(long rowId) 
    {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID 
                + "=" + rowId, null) > 0;
    }

    public void readDeadline() 
    {
        Log.i(TAG, "Reading the deadline from DB");

        this.open();

        Cursor c =  mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,
            KEY_TIME, KEY_DEADLINE, KEY_LEVEL},
                null, null, null, null, null);

        int timeIndex = c.getColumnIndex(KEY_TIME);
        int deadlineIndex = c.getColumnIndex(KEY_DEADLINE);
        int levelIndex = c.getColumnIndex(KEY_LEVEL);

        int size = c.getCount();
        c.moveToFirst();
        long setTimeVal;

        for (int i = 0; i < size; i++)
        {
            setTimeVal = c.getLong(timeIndex);
            mSetTime = Calendar.getInstance();
            mSetTime.setTimeInMillis(setTimeVal);


            mDeadline = c.getInt(deadlineIndex);
            mLevel = c.getInt(levelIndex);

            mHasDeadline = true;
        }

        if (mHasDeadline)
        {

            String resStr = "deadline " 
                + mDeadline
                + " submitted at " 
                + mSDF.format(mSetTime.getTime()) 
                + " (" + mLevel + "%)";

            Log.i(TAG, "Read " + resStr);
        }

        this.close();
    }

    public Calendar getSetTime()
    {
        if (mHasDeadline)
            return mSetTime;
        else 
            return null;
    }

    public int getDeadline()
    {
        if (mHasDeadline)
            return mDeadline;
        else
            return -1;
    }

    public int getLevel()
    {
        if (mHasDeadline)
            return mLevel;
        else
            return -1;
    }

    public Calendar getDeadlineDate()
    {

        if (!mHasDeadline)
            return null;

        Calendar deadlineDate = (Calendar) mSetTime.clone();
        deadlineDate.add(Calendar.MINUTE, mDeadline);

        return deadlineDate;

    }
        
}


