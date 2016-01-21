package com.hd.cu.android_project2;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Applist extends Activity {
    private static final String COMMAND_LINE = "/system/bin/cat";
    private static final String PATH_CPU = "/proc/cpuinfo";
    private static final String PATH_MAX = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    private static final String PATH_MIN = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
    private static final String PATH_CUR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static Map<String, AppData> appDataMap = new HashMap<>();
    public long totalCPUtime = 0;
    public double totalTime = 0.0;
    private BatteryManager bm;
    private String[] wakeList = null;
    private Context context;
    private ListView appView;
    private TextView appInfo, batteryRemain;
    private BroadcastReceiver batteryPercent;
    private IntentFilter batteryFilter;
    private Button optimizeBtn, refreshBtn;
    private List<AppData> appList = new LinkedList<>();
    private PackageManager packageManager;

    //Get Max CPU
    public static String getCpuInfo() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {COMMAND_LINE, PATH_CPU};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    //Get Max CPU
    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {COMMAND_LINE, PATH_MAX};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            int test = in.read(re);
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    //Get Min CPU
    public static String getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {COMMAND_LINE, PATH_MIN};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    public static long getTotalCpuTime() { //
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    public static long getAppCpuTimeSpec(int pid) {
        String[] cpuInfos = null;
        try {
            pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.running_app_list);
        initViewById();
        refersh();
        batteryState();


    }

    private void wakeLockListGet() {
        ProcessBuilder cmd;
        String result = "";
        try {
            String[] args = {COMMAND_LINE, "/sys/power/wake_lock"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            //    BufferedReader reader = new BufferedReader(new InputStreamReader(
            //        new FileInputStream( "/sys/power/wake_lock" )), 1000);
            byte[] re = new byte[24];
            int test = in.read(re);
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
            System.out.println("wake_lock_data" + result + "fds");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void refersh() {

        appList = AppInfoGet();
        percentCalculate(appDataMap);
        RunningAppAdapter runningAppAdapter = new RunningAppAdapter(this, appList);
        appView.setAdapter(runningAppAdapter);
    }

    private void initViewById() {

        appView = (ListView) findViewById(R.id.lview_App);
        appInfo = (TextView) findViewById(R.id.tv_App_Title);
        batteryRemain = (TextView) findViewById(R.id.tv_Remain_Battery);
        refreshBtn = (Button) findViewById(R.id.btn_Refresh);
        optimizeBtn = (Button) findViewById(R.id.btn_Optimize);
        packageManager = this.getPackageManager();
        optimizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wakeLockListGet();
                ClearDialog();

            }

        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refersh();
            }
        });
    }

    private void ClearDialog() {
        new AlertDialog.Builder(Applist.this)
                .setTitle("Do you want clean clean useless process")
                .setNegativeButton("Clean",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                cleanUselessPid();
                                if (!isWiFiAccessible(context)) {
                                    wifiDialog();
                                }

                            }
                        }).setPositiveButton("No", null).create()
                .show();
    }

    private void cleanUselessPid() {
        //To change body of implemented methods use File | Settings | File Templates.
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);

        long beforeMem = getAvailMemory(Applist.this);

        int count = 0;
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                ActivityManager.RunningAppProcessInfo appProcessInfo = infoList.get(i);


                if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; ++j) {
                        am.killBackgroundProcesses(pkgList[j]);
                        count++;
                    }
                }

            }
        }

        long afterMem = getAvailMemory(Applist.this);

        Toast.makeText(Applist.this, "clear " + count + " process, "
                + (afterMem - beforeMem) + "M", Toast.LENGTH_LONG).show();


    }

    private long getAvailMemory(Context context) {
        // get android the current available memory
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        //mi.availMem; The current available memory
        return memoryInfo.availMem / (1024 * 1024);
    }

    private void wifiDialog() {
        new AlertDialog.Builder(Applist.this)
                .setTitle("There is WiFi Available, Try close cellular ")
                .setNegativeButton("Setting",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                            }
                        }).setPositiveButton("cancel", null).create()
                .show();
    }

    private boolean isGpsEnable() {
        LocationManager locationManager =
                ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void batteryState() {
        batteryPercent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                StringBuilder stringBuilder = new StringBuilder();
                int batteryPer = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int status = intent.getIntExtra("status", -1);
                int health = intent.getIntExtra("health", -1);
                int percent = (batteryPer * 100) / scale;
                stringBuilder.append("Battery");
                stringBuilder.append(percent + "%");
//                switch (status) {
//                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
//                        stringBuilder.append("[No Battery!]");
//                        break;
//                    case BatteryManager.BATTERY_STATUS_CHARGING:
//                        stringBuilder.append("[Charging...]");
//                        break;
//                    default:
//                        if(percent <= 20)
//                            stringBuilder.append("[Battery Low!!]");
//                        else if (percent <= 100) {
//                            stringBuilder.append("[Unconnected with charger]");
//                        }
//                        break;
//
//                }
                batteryRemain.setText(stringBuilder.toString());
            }
        };
        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryPercent, batteryFilter);

    }

    private void percentCalculate(Map<String, AppData> appDataMap) {

        long totaltime = getTotalCpuTime();
        for (AppData appData : appDataMap.values()) {
            totalTime += appData.getCpuTime();
        }
        for (AppData appData : appDataMap.values()) {
            // appData.setPercent((getAppCpuTimeSpec(appData.getPid())/totaltime)*100);
            appData.setPercent(100 * appData.getCpuTime() / totalTime);
        }
    }

    private List<AppData> AppInfoGet() {
        Map<String, ActivityManager.RunningAppProcessInfo> appMap = new HashMap<>();
        PackageManager pm = this.getPackageManager();
        List<ActivityManager.RunningAppProcessInfo> appRunList = new ArrayList<>();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        appRunList = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo app : appRunList) {
            int pid = app.pid;
            String process = app.processName;
            String[] packList = app.pkgList;
            for (int i = 0; i < packList.length; i++) {
                appMap.put(packList[i], app);
            }
        }

        List<AppData> resultApp = new LinkedList<>();
        long targetCpuTime = 0;
        for (ApplicationInfo appInfo : pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)) {
            ActivityManager.RunningAppProcessInfo appDataInfo = appMap.get(appInfo.packageName);
            if (appDataInfo != null) {
                totalCPUtime += getAppProcessTime(appDataInfo.pid);
            }


            if (appMap.containsKey(appInfo.packageName)) {
                targetCpuTime += getAppProcessTime(appDataInfo.pid);
                resultApp.add(setAppInfo(appInfo, appDataInfo.pid, context, appDataInfo.processName, am, targetCpuTime));
            }
        }

        return resultApp;
    }

    private AppData setAppInfo(ApplicationInfo appInfo, int pid, Context context, String processName, ActivityManager am, long targetCpuTime) {

        AppData appData = new AppData();
        DataSetting(appData, context, appInfo, pid, processName, am, targetCpuTime);

        appDataMap.put(appData.getPackageName(), appData);
        return appData;
    }

    private void DataSetting(AppData appData, Context context, ApplicationInfo appInfo, int pid, String processName, ActivityManager am, long targetCpuTime) {

        appData.setAppFig(appInfo.loadIcon(packageManager));
        appData.setAppName((String) appInfo.loadLabel(packageManager));
        appData.setPid(pid);

        int[] memoryPid = new int[]{pid};
        Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(memoryPid);
        int memory = memoryInfo[0].dalvikPrivateDirty;
        appData.cpuTime = targetCpuTime;
        appData.setMemory(memory);
        appData.setProcess(processName);
        appData.setPackageName(appInfo.packageName);
        getAllAppCPUTime();

    }

    protected double[] getIA() {
        int speedSteps = 10;
        double[] powerCpuNormal = new double[speedSteps];
        for (int i = 0; i < speedSteps; i++) {
            powerCpuNormal[i] = 100 * (i + 1);
        }
        return powerCpuNormal;
    }

    private long getAppProcessTime(int pid) {
        FileInputStream in = null;
        String ret = null;
        try {
            in = new FileInputStream("/proc/" + pid + "/stat");
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            ret = os.toString();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ret == null) {
            return 0;
        }

        String[] s = ret.split(" ");
        if (s == null || s.length < 17) {
            return 0;
        }

        long utime = string2Long(s[13]);
        long stime = string2Long(s[14]);
        long cutime = string2Long(s[15]);
        long cstime = string2Long(s[16]);

        return utime + stime + cutime + cstime;
    }

    private long string2Long(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    private List<AppData> getAllAppCPUTime() {
        List<AppData> resultList = new LinkedList<>();
        long totalTimeCost = 0;
        long time = 0;
        long total = getTotalCpuTime();
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = activityManager.getRunningAppProcesses();
        HashMap<String, AppData> map = new HashMap<>();
        timeUpdate(appList, time, map, totalTimeCost);
        if (totalTimeCost == 0) totalTimeCost += 1;
        resultList.addAll(map.values());
        Iterator itor = resultList.iterator();
        while (itor.hasNext()) {
            AppData appData = (AppData) itor.next();
            appData.setPercent(appData.getTime() * 100 / total);
            //       appData.setPercent(getAppCpuTimeSpec(appData.getPid())*100/total);
        }
        return resultList;
    }

    private void timeUpdate(List<ActivityManager.RunningAppProcessInfo> appList, long time, HashMap<String, AppData> map, long totalTimeCost) {
        for (ActivityManager.RunningAppProcessInfo appInfo : appList) {
            time = getSepAppProcessTime(appInfo.pid);
            String[] packageName = appInfo.pkgList;
            if (packageName != null) {
                for (String pakName : packageName) {
                    if (!map.containsKey(pakName)) {
                        map.put(pakName, new AppData(context, pakName, time));
                    } else {
                        AppData appData = map.get(appInfo.processName);
                        if (appData != null)
                            appData.setCpuTime((double) appData.getCpuTime() + time);

                        //           appData.setTime((long)appData.getTime()+time);
                    }
                    totalTimeCost += time;
                }
            } else {
                if (!map.containsKey(appInfo.processName)) {
                    map.put(appInfo.processName, new AppData(context, appInfo.processName, time));
                } else {
                    AppData appData = map.get(appInfo.processName);
                    //          appData.setTime((long)appData.getTime()+time);
                    appData.setCpuTime(appData.getCpuTime() + time);
                }
                totalTimeCost += time;
            }
        }
    }

    private long getSepAppProcessTime(int pid) {
        int len = 0;
        String result = null;
        String[] time;
        try {
            FileInputStream fileInputStream = new FileInputStream("/proc/" + pid + "/stat");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] temp = new byte[4096];

            for (; ; ) {
                if ((len = fileInputStream.read(temp)) == -1) break;
                outputStream.write(temp, 0, len);
            }
            result = outputStream.toString();
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null) return 0;
        time = result.trim().split(" ");
        if (time.length < 17) return 0;
        return Long.parseLong(time[13]) + Long.parseLong(time[14]) + Long.parseLong(time[15]) + Long.parseLong(time[16]);
    }

    public boolean isWiFiAccessible(Context inContext) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


}
