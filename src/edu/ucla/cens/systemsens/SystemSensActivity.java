package edu.ucla.cens.systemsens;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;


import edu.ucla.cens.systemsens.util.Status;

import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONException;


public class SystemSensActivity extends Activity {
	
	private static final String TAG = "SystemSensActivity";

    private IPowerMonitor mSystemSens;
    private boolean mIsBound = false;

    private int mDeadline;

    private TextView mValText;
    private TextView mStatusText;
    private SeekBar mSeekBar;
    private Button mSubmitButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Vibrator vib = (Vibrator)
            getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        */


        setContentView(R.layout.main);

        startService(new Intent(this, SystemSens.class));


        mSeekBar = (SeekBar) findViewById(R.id.SeekBar);

        mValText = (TextView) findViewById(R.id.Value);
        mValText.setText("Scroll to set battery deadline.\n");




        mStatusText = (TextView) findViewById(R.id.Status);




        mSubmitButton = (Button) findViewById(R.id.Submit);
        mSubmitButton.setOnClickListener(mSubmitListener);

        
        if (!mIsBound)
            bindService(new Intent(IPowerMonitor.class.getName()), 
                    mSystemSensConnection,
                    Context.BIND_AUTO_CREATE);


        if (Status.isPlugged())
        {
            mValText.setText("Set battery deadline after charging.");
            mSeekBar.setOnTouchListener(new OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        return true;
                    }
                }
            );
        }
        else
        {
            mSeekBar.setOnSeekBarChangeListener(mSBListener);
        }



    }

    @Override
    public void onPause()
    {

        super.onPause();

        if (mIsBound)
        {
            unbindService(mSystemSensConnection);
            mIsBound = false;
        }
        else
        {
            Log.i(TAG, "Flag is not set.");
        }



    }


    private ServiceConnection mSystemSensConnection =  
        new ServiceConnection()
    {
        public void onServiceConnected(ComponentName classname,
                IBinder service)
        {
            mSystemSens = IPowerMonitor.Stub.asInterface(service);
                
            mIsBound = true;
            Log.i(TAG, "Got SystemSens object");

            readStatus();

        }

        public void onServiceDisconnected(ComponentName className)
        {
            mIsBound = false;
            mSystemSens = null;

        }
    };


    private OnClickListener mSubmitListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            if (mIsBound)
            {
                try
                {
                    mSystemSens.setDeadline(mDeadline);
                    Log.i(TAG, "Setting battery deadline to " + 
                            mDeadline);
                }
                catch (RemoteException re)
                {
                    Log.e(TAG, "Could not set deadline", re);
                }
                //Status.setDeadline(mDeadline);
            }
            else
            {
                Log.i(TAG, "Not connected to SystemSens.");
            }

            SystemSensActivity.this.finish();

        }
    };

    private OnSeekBarChangeListener  mSBListener = 
        new OnSeekBarChangeListener()
    {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser)
        {
           mValText.setText("Set battery deadline to: \n" 
                    + Status.deadlineStr(progress));
           mDeadline = progress;

        }



    };


    private void readStatus()
    {
        mStatusText.setText(Status.getString());
        double curDeadline =  Status.getDeadline();
        if (!Double.isNaN(curDeadline))
        {
            mDeadline = (int)curDeadline;
            mSeekBar.setMax((int)(Status.getLevel()*14.4));
            mSeekBar.setProgress(mDeadline);
        }

    }
}
