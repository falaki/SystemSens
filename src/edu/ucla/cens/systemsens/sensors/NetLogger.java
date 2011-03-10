package edu.ucla.cens.systemsens.sensors;

import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.TrafficStats;
import android.util.Log;
import android.os.Build;

import edu.ucla.cens.systemsens.util.Status;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Responsible for monitoring the amount of network traffic that has been made
 * by each application. It stores this information into the database.
 * 
 * There should not exist an instance of this class. Instead, its constructor
 * should be called exactly once before the static methods of this class are
 * used. All future interactions should be done through the static methods.
 * 
 * @author Hossein Falaki and John Jenkins
 * @version 1.0
 */
public class NetLogger 
{
	private static final String TAG = "NetworkProcessor";
	
	private static final String CAT_CMD = "/system/bin/cat";
	
    /** Address of the network devices stat */
    private static final String NETDEV_PATH  = "/proc/net/dev";
	
	private static Context mContext;

    private static final double MB = 1048576.0;


	
	
	
	/**
	 * Creates a NetworkProcessor object. It needs a Context to monitor the
	 * installed applications' network usage.
	 * 
	 * @param context The Context in which this object was created.
	 */
	public NetLogger(Context context)
	{
		mContext = context;
	}
	
	/**
	 * Updates the database with the current network statistics collected by
	 * the system.
	 */
	public JSONObject getAppNetUsage()
	{
		
		// Get all UIDs
		List<ApplicationInfo> apps = mContext.getPackageManager().getInstalledApplications(0);
		int appsLen = apps.size();

		JSONObject data, result = new JSONObject();
		
		// Update the database with the necessary information.
		ApplicationInfo currApp = null;
		long currRxBytes, currTxBytes;
		for(int i = 0; i < appsLen; i++)
		{
			currApp = apps.get(i);

			currRxBytes = TrafficStats.getUidRxBytes(currApp.uid);
			currTxBytes = TrafficStats.getUidTxBytes(currApp.uid);
			
			if((currRxBytes != TrafficStats.UNSUPPORTED) || (currTxBytes != TrafficStats.UNSUPPORTED))
			{
                try
                {
                	data = new JSONObject();
                    data.put("Rx", currRxBytes);
                    data.put("Tx", currTxBytes);
                    
                    result.put(currApp.packageName, data);

                }
                catch (JSONException je)
                {
                    Log.e(TAG, "Exception", je);
                }

			}
		}
		
		return result;
	}
	
	public JSONObject getIfNetUsage()
	{
	
		JSONObject result = new JSONObject();
		
		try
		{
			
			result.put("MobileRxBytes", 
                    TrafficStats.getMobileRxBytes());
			result.put("MobileTxBytes", 
                    TrafficStats.getMobileTxBytes());
			result.put("MobileRxPackets", 
                    TrafficStats.getMobileRxPackets());
			result.put("MobileTxPackets", 
                    TrafficStats.getMobileTxPackets());
			
			result.put("TotalRxBytes", 
                    TrafficStats.getTotalRxBytes());
			result.put("TotalTxBytes", 
                    TrafficStats.getTotalTxBytes());
			result.put("TotalRxPackets", 
                    TrafficStats.getTotalRxPackets());
			result.put("TotalTxPackets", 
                    TrafficStats.getTotalTxPackets());
		}
        catch (JSONException je)
        {
            Log.e(TAG, "Exception", je);
        }

        Status.setTraffic(TrafficStats.getTotalTxBytes()/MB,
                TrafficStats.getTotalRxBytes()/MB);

		
		return result;
		
	}
	
    /**
     * Parses and returns the contents of /proc/net/dev.
     * It first reads the content of the file in /proc/net/dev. 
     * This file contains a row for each network interface. 
     * Each row contains the number of bytes and packets that have
     * been sent and received over that network interface. This method
     * parses this file and returns a JSONObject that maps the network
     * interface name to this information.
     *
     * @return          JSONObject containing an entry for each
     *                      physical interface. 
     */
    public JSONObject getNetDev()
    {
	

        JSONObject result = new JSONObject();
        JSONObject data;
        StringTokenizer linest;
        String devName, recvBytes, recvPackets, 
               sentBytes, sentPackets, zero;

        String[] args = {CAT_CMD, NETDEV_PATH};
        ProcessBuilder cmd;

        try
        {
           
            cmd = new ProcessBuilder(args);
      
            Process process = cmd.start();
            InputStream devstream = process.getInputStream();



            byte[] buffer = new byte[2024];
            int readlen  = devstream.read(buffer);

            if ( readlen <= 0)
            {
                Log.e(TAG, "Could not read /proc/net/dev");
                return result;
            }
            
            StringTokenizer st = new StringTokenizer(
                    new String(buffer), "\n", false);

            //The first two lines of the file are headers
            zero = st.nextToken();
            zero = st.nextToken();

            for (int j = 0; j < 5; j++)
            {
                linest = new StringTokenizer(st.nextToken());
                devName = linest.nextToken();
                recvBytes = linest.nextToken();
                recvPackets = linest.nextToken();


                // Skip six tokens
                for (int i = 0; i < 6; i++) 
                    zero = linest.nextToken();

                sentBytes = linest.nextToken();
                sentPackets = linest.nextToken();



                data = new JSONObject();

                try
                {
                    data.put("RxBytes", recvBytes);
                    data.put("RxPackets", recvPackets);

                    data.put("TxBytes", sentBytes);
                    data.put("TxPackets", sentPackets);
                    
                    result.put(devName, data);

                }
                catch (JSONException je)
                {
                    Log.e(TAG, "Exception", je);
                }

            }

        }
        catch (Exception e)
        {

            Log.e(TAG, "Exception", e);
        }


        return result;
    }
}
