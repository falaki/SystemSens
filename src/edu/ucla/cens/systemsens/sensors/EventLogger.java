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


package edu.ucla.cens.systemsens.sensors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.EventLog;
import android.util.Log;
import android.util.EventLog.Event;
import android.os.Build;

/**
 * Reads the logs for which applications have been started, restarted, 
 * resumed, or paused to get an idea for what applications have been used.
 * 
 * There should never exist an instance of this class. Its constructor
 * should be called exactly once to initialize the class. All
 * subsequent calls should be to the static functions.
 * 
 * @author Hossein Falaki and John Jenkins
 * @version 1.0
 */
public class EventLogger 
{
	private static final String TAG = "ActivityLogger";
	
	private static final String EVENT_LOG_TAGS_FILE = "/system/etc/event-log-tags";
	

	
	
	private Set<String> mActivityEvents;
	private Set<String> mCpuEvents;
	private Set<String> mServiceEvents;
	private Set<String> mMemoryEvents;

	private HashMap<Integer, TagInfo> mTags;
	
	private long mLastUpdate;
	
	private JSONObject mActivity, mService, mCpu, mMem;
	
	/**
	 * Creates a new EventProcessor object. 
	 */
	public EventLogger()
	{			

		mTags = new HashMap<Integer, TagInfo>();
		
		mActivityEvents = new HashSet<String>();
		mCpuEvents = new HashSet<String>();
		mServiceEvents = new HashSet<String>();
		mMemoryEvents = new HashSet<String>();
		


		mActivityEvents.add("am_create_activity");
		mActivityEvents.add("am_restart_activity");
		mActivityEvents.add("am_resume_activity");
		mActivityEvents.add("am_pause_activity");
		mActivityEvents.add("am_destroy_activity");
		mActivityEvents.add("am_relaunch_activity");
		mActivityEvents.add("am_finish_activity");

		

		mServiceEvents.add("am_create_service");
		mServiceEvents.add("am_destroy_service");

		
		mCpuEvents.add("cpu");
		
		mMemoryEvents.add("watchdog_meminfo");
		
		
		mLastUpdate = 0L;
		
		getTags();

	}
	
	/**
	 * Reads the event log tags file to populate the internal information
	 * about the structure of the logs. This information is then used later
	 * when retrieving logging information.
	 * 
	 * This file follows the following format:
	 * 		<tag_id> <tag_name> <tag_descriptor>
	 * 
	 * We parse this file based on the 'tag_name's we want to get the
     * 'tag_id' and 'tag_descriptor'. We then further parse the
     * 'tag_descriptor' to get the specific descriptor we want. Android
     * provides a similar function that parses this file, based on a
     * 'tag_name', for the 'tag_id', but it is very slow; therefore, we
     * do it once and remember it to speed things up. For more
     * information see 'readAndParseFile()' and 'getComponentIndex()'.
	 * 
	 * @see readAndParseFile
	 * @see getComponentIndex
	 */
	private void getTags()
	{

		File tagsFile = new File(EVENT_LOG_TAGS_FILE);
		
		if(tagsFile == null)
		{
			Log.e(TAG, "Failed to connect to event logs tags file.");
			return;
		}
		else if(!tagsFile.exists())
		{
			Log.e(TAG, "There is no event logs in tags file.");
			return;
		}
		else if(!tagsFile.canRead())
		{
			Log.e(TAG, "Cannot read the event logs tags file.");
			return;
		}
		else if(tagsFile.length() == 0L)
		{
			Log.e(TAG, "The logs tags file claims to have a length of zero.");
			return;
		}
		
		readAndParseFile(tagsFile);		
	}
	
	/**
	 * Parses the event log tags file by finding the relevant tags and
	 * populating the local information with the structure of these logs.
	 * 
	 * @param theFile A File object that should already be attached to the
	 * 				  event log tags file.
	 */
	private void readAndParseFile(File theFile)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new
                    FileReader(theFile));
			String currLine;
			ActivityTagInfo appTagInfo;
			CpuTagInfo cpuTagInfo;
			ServiceTagInfo serviceTagInfo;
			MemTagInfo memTagInfo;
			
			while ((currLine = reader.readLine()) != null)
			{
				
				String[] lineInfo = currLine.split(" ", 3);
				String currType = lineInfo[1];
				
				
				if (mActivityEvents.contains(currType))
				{
					appTagInfo = new ActivityTagInfo(currType);
					
					appTagInfo.id = Integer.parseInt(lineInfo[0]);
					appTagInfo.componentNameIndex =
                        getIndex(lineInfo[2], "Component Name");
					appTagInfo.actionIndex = getIndex(lineInfo[2],
                            "Action");
					appTagInfo.taskIndex = getIndex(lineInfo[2], 
                            "Task ID");
					
					mTags.put(appTagInfo.id, appTagInfo);
					
				}
				else if (mServiceEvents.contains(currType))
				{
					serviceTagInfo = new ServiceTagInfo(currType);
					
					serviceTagInfo.id = Integer.parseInt(lineInfo[0]);
					
					serviceTagInfo.recordIndex = getIndex(lineInfo[2],
                            "Service Record");
					serviceTagInfo.nameIndex = getIndex(lineInfo[2],
                            "Name");
					serviceTagInfo.pidIndex = getIndex(lineInfo[2], "PID");
					serviceTagInfo.intentIndex = getIndex(lineInfo[2],
                            "Intent");
					
					mTags.put(serviceTagInfo.id, serviceTagInfo);
				}
				else if (mCpuEvents.contains(currType))
				{
					cpuTagInfo = new CpuTagInfo(currType);
					
					cpuTagInfo.id = Integer.parseInt(lineInfo[0]);
					cpuTagInfo.totalIndex = getIndex(lineInfo[2], "total");
					cpuTagInfo.userIndex = getIndex(lineInfo[2], "user");
					cpuTagInfo.iowaitIndex = getIndex(lineInfo[2],
                            "iowait");
					cpuTagInfo.irqIndex = getIndex(lineInfo[2], "irq");
					cpuTagInfo.softirqIndex = getIndex(lineInfo[2],
                            "softirq");
					
					mTags.put(cpuTagInfo.id, cpuTagInfo);
					
				}
				else if (mMemoryEvents.contains(currType))
				{
					memTagInfo = new MemTagInfo(currType);
					
					memTagInfo.id = Integer.parseInt(lineInfo[0]);
					memTagInfo.memFreeIndex = getIndex(lineInfo[2],
                            "MemFree");
					memTagInfo.buffersIndex = getIndex(lineInfo[2],
                            "Buffers");
					memTagInfo.cachedIndex = getIndex(lineInfo[2],
                            "Cached");
					memTagInfo.activeIndex = getIndex(lineInfo[2],
                            "Active");
					memTagInfo.inactiveIndex = getIndex(lineInfo[2],
                            "Inactive");
					memTagInfo.anonPagesIndex = getIndex(lineInfo[2],
                            "NaonPages");
					memTagInfo.mappedIndex = getIndex(lineInfo[2],
                            "Mapped");
					memTagInfo.slabIndex = getIndex(lineInfo[2], "Slab");
					memTagInfo.sReclaimableIndex =
                        getIndex(lineInfo[2], "SReclaimable");
					memTagInfo.sUnreclaimableIndex =
                        getIndex(lineInfo[2], "SUnreclaimable");
					
					
				}
					
				
			}
		}
		catch(FileNotFoundException e)
		{
			Log.e(TAG, "The event logs tags file was not found.");
		}
		catch(IOException e)
		{
			Log.e(TAG, "Couldn't read the entire event logs tags file.");
		}
	}
	
    /** Parses the component descriptor string to find the index among
     * the list of descriptions that matches "Component Name". This is
     * the value that is used by Android to describe the Application
     * and Activity in that Application and is the value we store in
     * the database to associate who is calling this event.  @param
     * componentDescriptor A String pulled from the event log tags
     * file that describes all the types of information that Android
     * records when an event takes place.
     * 
     * @return The index of the "Component Name" in the descriptor.
     */
	private int getIndex(String componentDescriptor, String component)
	{
		String[] params = componentDescriptor.split(",");
		int currIndex = 0;
		int compLen = component.length();
		while(currIndex < params.length)
		{
			try
			{
				if(params[currIndex].substring(1, 
                            compLen + 1).equals(component))
				{
					return currIndex;
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// This is expected as some descriptors aren't as long
                // as the one we need.
			}
			currIndex++;
		}
		
		return -1;
	}
		
	/**
	 * Updates the database with the events that have taken place since the
	 * last call to this function. 
	 */
	public void update()
	{

		
		mActivity = new JSONObject();
		mService = new JSONObject();
		mMem = new JSONObject();
		mCpu = new JSONObject();
		
		int[] tagsToRetrieve = new int[mTags.size()];
		
		int index = 0;
		for (TagInfo curTag : mTags.values())
			tagsToRetrieve[index++] = curTag.id;
		
		//Log.i(TAG, "Tags to retrive: " + tagsToRetrieve.length);
		
		Collection<EventLog.Event> log = new LinkedList<EventLog.Event>();
		
		try
		{
			EventLog.readEvents(tagsToRetrieve, log);
		}
		catch(IOException e)
		{
			Log.e(TAG, "Exception when trying to readEvents()", e);
			return;
		}
		
		long greatestEventTime = 0L;
		long eventTime;
		int eventID;
		
		TagInfo refTag;
		ActivityTagInfo appTag;
		ServiceTagInfo serviceTag;
		CpuTagInfo cpuTag;
		MemTagInfo memTag;
		
		Object[] data;
		
		String componentNameStr;
		String actionStr;
		Integer taskID;
		
		String serviceName = "";
		String serviceIntent = "";
		int servicePid;
		
		JSONObject eventObject;
		
		//Log.i(TAG, "Got " + log.size() + " events");
		
		for (Event currEvent : log)
		{
			eventTime = currEvent.getTimeNanos();
			eventID = currEvent.getTag();
			
			if (eventTime > mLastUpdate)
			{
				refTag = mTags.get(eventID);
				data = (Object[]) currEvent.getData();
				
				if(eventTime > greatestEventTime)
					greatestEventTime = eventTime;	
				
				if (refTag.type == TagInfo.ACTIVITY)
				{
					appTag = (ActivityTagInfo)refTag;
					
					componentNameStr = "";
					actionStr = "";
					taskID = 0;
					
		
					
					if (appTag.componentNameIndex != -1)
						componentNameStr = (String) ((Object[])
                                data)[appTag.componentNameIndex];
					
					if (appTag.actionIndex != -1)
						actionStr = (String) ((Object[])
                                data)[appTag.actionIndex];
					
					if (appTag.taskIndex != -1)
						taskID = (Integer) ((Object[])
                                data)[appTag.taskIndex];

					eventObject = new JSONObject();
					
					try
					{
						eventObject.put("Event", appTag.tagName);
						eventObject.put("Activity", componentNameStr);
						eventObject.put("Action", actionStr);
						eventObject.put("Task", taskID);
						
						mActivity.put(String.valueOf(eventTime),
                                eventObject);
					}
					catch (JSONException je)
					{
						Log.e(TAG, "JSON Exception", je);
					}

				} 
				else if (refTag.type == TagInfo.SERVICE)
				{
					
					serviceTag = (ServiceTagInfo) refTag;
					
					serviceName = (String) ((Object[])
                            data)[serviceTag.nameIndex];
					if (serviceTag.intentIndex != -1 )
						serviceIntent = (String) ((Object[])
                                data)[serviceTag.intentIndex];
					
					servicePid = (Integer) ((Object[])
                            data)[serviceTag.pidIndex];
					
					eventObject = new JSONObject();
					try
					{
						eventObject.put("Event", serviceTag.tagName);
						eventObject.put("Name", serviceName);
						eventObject.put("Intent", serviceIntent);
						eventObject.put("PID", servicePid);
						
						mService.put(String.valueOf(eventTime),
                                eventObject);
					}
					catch (JSONException je)
					{
						Log.e(TAG, "JSON Exception", je);
					}

					
				}
				else if (refTag.type == TagInfo.CPU)
				{
					cpuTag = (CpuTagInfo)refTag;
					
					long total, user, system, iowait, irq, softirq;
					
					total = (Integer) ((Object[]) data)[cpuTag.totalIndex];
					user = (Integer) ((Object[]) data)[cpuTag.userIndex];
					system = (Integer) ((Object[])
                            data)[cpuTag.systemIndex];
					iowait = (Integer) ((Object[])
                            data)[cpuTag.iowaitIndex];
					irq = (Integer) ((Object[]) data)[cpuTag.irqIndex];
					softirq = (Integer) ((Object[])
                            data)[cpuTag.softirqIndex];
					
					eventObject = new JSONObject();
					try
					{
						eventObject.put("total", total);
						eventObject.put("user", user);
						eventObject.put("system", system);
						eventObject.put("iowait", iowait);
						eventObject.put("irq", irq);
						eventObject.put("softirq", softirq);
						
						mService.put(String.valueOf(eventTime),
                                eventObject);
					}
					catch (JSONException je)
					{
						Log.e(TAG, "JSON Exception", je);
					}
					
				}
				else if (refTag.type == TagInfo.MEMORY)
				{
					memTag = (MemTagInfo) refTag;
					
					long memFree, buffers, cached, active, 
							inactive, anonpages, mapped, 
							slab, sreclaimable, sunreclaimable;
					
					memFree = (Long) ((Object[]) data)[memTag.memFreeIndex];
					buffers = (Long) ((Object[]) data)[memTag.buffersIndex];
					cached = (Long) ((Object[]) data)[memTag.cachedIndex];
					active = (Long) ((Object[]) data)[memTag.activeIndex];
					inactive  = (Long) ((Object[])
                            data)[memTag.inactiveIndex];
					anonpages = (Long) ((Object[])
                            data)[memTag.anonPagesIndex];
					mapped = (Long) ((Object[]) data)[memTag.mappedIndex];
					slab = (Long) ((Object[]) data)[memTag.slabIndex];
					sreclaimable = (Long) ((Object[])
                            data)[memTag.sReclaimableIndex];
					sunreclaimable = (Long) ((Object[])
                            data)[memTag.sUnreclaimableIndex];
					
					
					eventObject = new JSONObject();
					try
					{
						eventObject.put("free", memFree);
						eventObject.put("buffers", buffers);
						eventObject.put("cached", cached);
						eventObject.put("active", active);
						eventObject.put("inactive", inactive);
						eventObject.put("anonpages", anonpages);
						eventObject.put("mapped", mapped);
						eventObject.put("slab", slab);
						eventObject.put("sreclaimable", sreclaimable);
						eventObject.put("sunreclaimable", sunreclaimable);
						
						
						mService.put(String.valueOf(eventTime),
                                eventObject);
					}
					catch (JSONException je)
					{
						Log.e(TAG, "JSON Exception", je);
					}

				}

			}
			
		}
		
		
		if (greatestEventTime != 0)
		{
			mLastUpdate = greatestEventTime;
		}
	}
	
	public JSONObject getActivityEvents()
	{

		return mActivity;
	}
	
	public JSONObject getServiceEvents()
	{


		return mService;
	}
	
	public JSONObject getCpuEvents()
	{

		return mCpu;
	}
	
	public JSONObject getMemEvents()
	{

		return mMem;
	}
	
}


class TagInfo
{
	public String tagName;
	public int id;
	public int type;
	
	public final static int ACTIVITY = 1;
	public final static int SERVICE = 2;
	public final static int CPU = 3;
	public final static int MEMORY = 4;
	
	public TagInfo(String name, int inType)
	{
		tagName = name;
		type = inType;
		id = -1;
	}
}

class ActivityTagInfo extends TagInfo
{
	public int componentNameIndex;
	public int actionIndex;
	public int taskIndex;
	
	public ActivityTagInfo(String name)
	{
		super(name, TagInfo.ACTIVITY);
		componentNameIndex = actionIndex = taskIndex = -1;
	}
}

class ServiceTagInfo extends TagInfo
{
	public int recordIndex;
	public int nameIndex;
	public int intentIndex;
	public int pidIndex;
	
	public ServiceTagInfo(String name)
	{
		super(name, TagInfo.SERVICE);
		recordIndex = nameIndex = intentIndex = pidIndex = -1;
	}
	
}

class CpuTagInfo extends TagInfo
{
	public int totalIndex;
	public int userIndex;
	public int systemIndex;
	public int iowaitIndex;
	public int irqIndex;
	public int softirqIndex;
	
	public CpuTagInfo(String name)
	{
		super(name, TagInfo.CPU);
		totalIndex = userIndex = systemIndex = iowaitIndex = irqIndex = softirqIndex = -1;
	}
}

class MemTagInfo extends TagInfo
{
	public int memFreeIndex = -1;
	public int buffersIndex = -1;
	public int cachedIndex = -1;
	public int activeIndex = -1;
	public int inactiveIndex = -1;
	public int anonPagesIndex = -1;
	public int mappedIndex = -1;
	public int slabIndex = -1;
	public int sReclaimableIndex = -1;
	public int sUnreclaimableIndex = -1;
	public int pageTablesIndex = -1;
	
	public MemTagInfo(String name)
	{
		super(name, TagInfo.MEMORY);
	}
	
}
