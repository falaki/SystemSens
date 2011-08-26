package edu.ucla.cens.systemsens.sensors;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.os.Debug.MemoryInfo;
import android.util.Log;
import android.content.Intent;
import android.content.ComponentName;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;


public class ActivityLogger
{
    ActivityManager mActivityManager;

    private static final String TAG = "SystemSensActivityLogger";
    private static final int MAX_NUM = 100;

    private long mTotalCpu;
    private HashMap<Integer, List<Long>> mTimeMap;


    public ActivityLogger(Context context)
    {
        mActivityManager = (ActivityManager)
            context.getSystemService(Context.ACTIVITY_SERVICE);

        mTotalCpu = Proc.getCpuTotalTime();
        mTimeMap = new HashMap<Integer, List<Long>>();

    }


    public JSONObject getRecentTasks()
    {
        JSONObject result = new JSONObject();

        List<RecentTaskInfo> recentTasks =
            mActivityManager.getRecentTasks(MAX_NUM,
                    ActivityManager.RECENT_WITH_EXCLUDED);


        JSONObject processJson;
        ComponentName component;

        for (RecentTaskInfo task : recentTasks)
        {
            processJson = new JSONObject();
            try
            {
                component = task.baseIntent.getComponent();
                if (component != null)
                    processJson.put("ComponentName",
                            component.getClassName());

                component = task.origActivity;
                if (component != null)
                    processJson.put("OrigActivity",
                            component.getClassName());


                /* Available in API Level 11 
                processJson.put("Description",
                        task.description.toString());
                */
                result.put(Integer.toString(task.id), processJson);
            }
            catch (JSONException je)
            {
                Log.e("TAG", "Could not insert data into JSONObject",
                        je);
            }


        }

        return result;



    }


    public JSONObject getMemCpu()
    {
        JSONObject result = new JSONObject();

        List<RunningAppProcessInfo> runningApps = 
            mActivityManager.getRunningAppProcesses(); 
        Map<Integer, String> pidMap = new TreeMap<Integer, String>(); 
        
        for (RunningAppProcessInfo appInfo : runningApps) 
        { 
            pidMap.put(appInfo.pid, appInfo.processName); 
        } 

        int[] pids = new int[pidMap.size()];

        int index = 0;
        for (Integer id : pidMap.keySet())
        {
            pids[index++] = id;
        }
        MemoryInfo[] memInfo = mActivityManager.getProcessMemoryInfo(pids); 

        long curTotalCpuTime = Proc.getCpuTotalTime();
        long jiffies = curTotalCpuTime - mTotalCpu;
        mTotalCpu = curTotalCpuTime;

        int pid;
        List<Long> cpuTime, lastCpuTime;
        double uCpu, sCpu;
        
        long lastUTime, lastSTime, uTime, sTime;

        MemoryInfo info;
        JSONObject processJson;

        for(int i = 0; i < pids.length; i++)
        { 
            pid = pids[i];
            info = memInfo[i];
            processJson = new JSONObject();

            cpuTime = Proc.readProcessCpuTime(pid);
            if (cpuTime != null)
            {
                uTime = cpuTime.get(0);
                sTime = cpuTime.get(1);
            }
            else
            {
                uTime = 0L;
                sTime = 0L;
            }


            if (mTimeMap.containsKey(pid))
            {
                lastCpuTime = mTimeMap.get(pid);
                lastUTime = lastCpuTime.get(0);
                lastSTime = lastCpuTime.get(1);
           }
            else
            {
                lastUTime = 0L;
                lastSTime = 0L;

            }

            uCpu = 100.0f * (uTime - lastUTime) / jiffies;
            sCpu = 100.0f * (sTime - lastSTime) / jiffies;

            mTimeMap.put(pid, cpuTime);



            try
            {
                processJson.put("TotalPss", info.getTotalPss());
                processJson.put("PrivateDirty",
                        info.getTotalPrivateDirty());
                processJson.put("SharedDirty",
                        info.getTotalSharedDirty());

                processJson.put("UserCpu", uCpu);
                processJson.put("SystemCpu", sCpu);

                result.put(pidMap.get(pid), processJson);
            } 
            catch (JSONException je)
            {
                Log.e("TAG", "Could not insert data into JSONObject",
                        je);
            }
        }

        return result;
    }
}
