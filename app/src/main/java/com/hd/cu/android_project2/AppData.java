package com.hd.cu.android_project2;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

public class AppData {
    public long usageTime;
    public double cpuTime = 0;
    public long gpsTime;
    public double percent;
    public long wifiRunningTime;
    public long cpuFgTime;
    public long wakeLockTime;
    public long tcpBytesReceived;
    public long tcpBytesSent;
    private String appName;
    private Drawable appFig;
    private double energy;
    private int pid;
    private String process;
    private String packageName;
    private DrainType drainType;
    private int memory;
    private Context context;
    private double[] cost;
    private double time;
    private String defaultPackageName;

    private Map<String, UidInfo> uidInfoMap = new HashMap<>();

    public AppData(Context context, String pakName, long time) {
        this.context = context;
        this.time = time;
        drainType = DrainType.APP;
    }


    public AppData() {

    }


    private void nameFigUpdate() {
        PackageManager pm = context.getPackageManager();
        int uidNum = 0;
        Drawable appIcon = pm.getDefaultActivityIcon();
        String[] packagesArray = pm.getPackagesForUid(uidNum);
        if (packagesArray == null) {
            appName = String.valueOf(uidNum);
            return;
        }
        String[] packageNameArray = new String[packagesArray.length];
        System.arraycopy(packagesArray, 0, packageNameArray, 0, packagesArray.length);
        for (int i = 0; i < packageNameArray.length; i++) {
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageNameArray[i], 0);
                CharSequence charSequence = applicationInfo.loadLabel(pm);
                if (charSequence != null && !charSequence.toString().equals(packageNameArray[i]))
                    packageNameArray[i] = charSequence.toString();
                if (applicationInfo.icon != 0) {
                    defaultPackageName = packageNameArray[i];
                    appFig = applicationInfo.loadIcon(pm);
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (appFig == null) appFig = appIcon;
        if (packageNameArray.length != 1) {
            for (String packageName : packagesArray) {
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                    if (packageInfo.sharedUserLabel != 0) {
                        CharSequence charSequence = pm.getText(packageName, packageInfo.sharedUserLabel, packageInfo.applicationInfo);
                        if (charSequence != null) {
                            appName = charSequence.toString();
                            if (packageInfo.applicationInfo.icon != 0) {
                                defaultPackageName = packageName;
                                appFig = packageInfo.applicationInfo.loadIcon(pm);
                            }
                            break;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            appName = packageNameArray[0];
        }
        String uidName = String.valueOf(1);
        UidInfo uidData = new UidInfo();
        uidData.name = appName;
        uidData.icon = appFig;
        uidData.packageName = defaultPackageName;
        uidInfoMap.put(uidName, uidData);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public Drawable getAppFig() {
        return appFig;
    }

    public void setAppFig(Drawable appFig) {
        this.appFig = appFig;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public double getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public double getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(double cpuTime) {
        this.cpuTime = cpuTime;
    }


}
