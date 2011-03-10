/**
 * SystemSens
 *
 * Copyright (C) 2009 Hossein Falaki
 */

package edu.ucla.cens.systemsens.sensors;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import edu.ucla.cens.systemsens.util.Status;



import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Reads varios information from the /proc file system. 
 *
 * After an object of this class is constructed, each call to
 * get*() methods returns a HashMap containing some information from
 * the /proc of the Linux kernel.
 * 
 * @author Hossein Falaki
 */
public class Proc
{
    /** TAG of this class for logging */
    private static final String TAG="SystemSens:Proc";


    /** Text string for the cat command */
    private static final String CAT_CMD = "/system/bin/cat";

    /** Address of the network devices stat */
    private static final String NETDEV_PATH  = "/proc/net/dev";
    
    /** Address of memory information file */
    private static final String MEMINFO_PATH = "/proc/meminfo";
    
    private long total = 0;
    private long idle  = 0;
    private long user = 0;
    private long system = 0;
    private long nice = 0;


    /**
     * Constructs a Proc object. 
     */
    public Proc()
    {
        // No initialization needed yet.
    	getCpuLoad();
    }
    
    public JSONObject getMemInfo()
    {
    	
    	JSONObject result = new JSONObject();
    	
        StringTokenizer linest;
        String key, value;

        try
        {

			BufferedReader reader = new BufferedReader( new 
					InputStreamReader( new FileInputStream( MEMINFO_PATH ) ), 2048 );


            char[] buffer = new char[2024];
            reader.read(buffer, 0, 2000);
            
            
            StringTokenizer st = new StringTokenizer(
                    new String(buffer), "\n", false);

            for (int i = 0; i < st.countTokens(); i++)
            {

                linest = new StringTokenizer(st.nextToken());
                key = linest.nextToken();
                value = linest.nextToken();

                try
                {
                	result.put(key, value);
                }
                catch (JSONException je)
                {
                    Log.e(TAG, "Exception", je);
                }
            }

        }
        catch (Exception e)
        {

            Log.e(TAG, "Exception parsing the file", e);
        }


        return result;    	
    }
    
    public JSONObject getCpuLoad()
    {
    	JSONObject result = new JSONObject();
    	
    	float totalUsage, userUsage, niceUsage, systemUsage;
        Double cpuFreq = 0.0;

    	String line;
    	String[] toks;
        String[] words;

        try
        {
			BufferedReader reader = new BufferedReader( new 
					InputStreamReader( new 
                        FileInputStream( "/proc/cpuinfo" ) ), 2048 );

			while ( (line = reader.readLine()) != null )
			{
				toks = line.split(" ");
                words = toks[0].split("\t");

                if (words[0].equals("BogoMIPS"))
                {
                    cpuFreq = Double.parseDouble(toks[1]);
                }
            }

        }
        catch (IOException ioe)
        {
            Log.e(TAG, "Exception parsing /proc/cpuinfo", ioe);
        }
    	
		try
		{
			BufferedReader reader = new BufferedReader( new 
					InputStreamReader( new 
                        FileInputStream( "/proc/stat" ) ), 2048 );

			while ( (line = reader.readLine()) != null )
			{
				toks = line.split(" ");
				
				if (toks[0].equals("cpu"))
				{
					long currUser, currNice, currSystem, currTotal, currIdle;
					
					JSONObject cpuObject = new JSONObject();
					
					currUser = Long.parseLong(toks[2]);
					currNice = Long.parseLong(toks[3]);
					currSystem = Long.parseLong(toks[4]);
					currTotal = currUser + currNice + currSystem;
					currIdle = Long.parseLong(toks[5]);
		 
					totalUsage = (currTotal - total) * 100.0f / 
                        (currTotal - total + currIdle - idle);
					userUsage = (currUser - user) * 100.0f / 
                        (currTotal - total + currIdle - idle);
					niceUsage = (currNice - nice) * 100.0f / 
                        (currTotal - total + currIdle - idle);
					systemUsage = (currSystem - system) * 100.0f / 
                        (currTotal - total + currIdle - idle);
					
					
					total = currTotal;
					idle = currIdle;
					user = currUser;
					nice = currNice;
					system = currSystem;

                    // Update the Status Object
                    Status.setCPU(totalUsage);
				
					try
					{
						cpuObject.put("total", totalUsage);
						cpuObject.put("user", userUsage);
						cpuObject.put("nice", niceUsage);
						cpuObject.put("system", systemUsage);
                        cpuObject.put("freq", cpuFreq);
						
						result.put("cpu", cpuObject);
						
					 }
		            catch (JSONException je)
		            {
		                Log.e(TAG, "Exception", je);
		            }
				} 
				else if (toks[0].equals("ctxt"))
				{
					String ctxt = toks[1];
					
					try
					{
						result.put("ContextSwitch", ctxt);
					}
		            catch (JSONException je)
		            {
		                Log.e(TAG, "Exception", je);
		            }
				}
				else if (toks[0].equals("btime"))
				{
					String btime = toks[1];
					
					try
					{
						result.put("BootTime", btime);
					}
		            catch (JSONException je)
		            {
		                Log.e(TAG, "Exception", je);
		            }
				}
				else if (toks[0].equals("processes"))
				{
					String procs = toks[1];
					
					try
					{
						result.put("Processes", procs);
					}
		            catch (JSONException je)
		            {
		                Log.e(TAG, "Exception", je);
		            }
				}				

			}
			
			reader.close();		
			
		}
		catch( IOException ex )
		{
			ex.printStackTrace();			
		}
		
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
     * @return          JSONObject containing en entry for each
     *                      physical interface. 
     */
    public JSONObject getNetDev()
    {

        JSONObject result = new JSONObject();
        JSONObject data;
        StringTokenizer linest;
        String devName, recvBytes, recvPackets, 
               sentBytes, sentPackets, zero;


        try
        {
                       
			BufferedReader reader = new BufferedReader( new 
					InputStreamReader( new FileInputStream( NETDEV_PATH ) ), 2048 );


            char[] buffer = new char[2024];
            reader.read(buffer, 0, 2000);
            
            
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

