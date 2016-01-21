package com.hd.cu.android_project2;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewData {
    public ImageView appIcon;
    public TextView tv_AppName;
    public TextView tv_memory;
    public TextView tv_process;
    public TextView tv_pid;
    public TextView tv_cpu;
    public TextView tv_percent;

    public ViewData(View view) {
        appIcon = (ImageView) view.findViewById(R.id.imv_ap_icon);
        tv_AppName = (TextView) view.findViewById(R.id.tv_app_name);
        tv_memory = (TextView) view.findViewById(R.id.tv_pkg_name);
        tv_process = (TextView) view.findViewById(R.id.tv_process);
        tv_pid = (TextView) view.findViewById(R.id.tv_pid);
        tv_cpu = (TextView) view.findViewById(R.id.tv_cpuTime);
        tv_percent = (TextView) view.findViewById(R.id.tv_percent);
    }

}
