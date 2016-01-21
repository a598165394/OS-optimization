package com.hd.cu.android_project2;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.LinkedList;
import java.util.List;

public class RunningAppAdapter extends BaseAdapter {

    private List<AppData> appList = new LinkedList<>();
    private LayoutInflater layoutInflater = null;
    private View view;
    private ViewData viewData;
    private AppData appData;

    public RunningAppAdapter(Context context, List<AppData> appList) {

        this.appList = appList;
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return appList.get(position).getPid();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            view = convertView;
            viewData = (ViewData) convertView.getTag();
        } else {
            view = layoutInflater.inflate(R.layout.app_info, null);
            viewData = new ViewData(view);
            view.setTag(viewData);
        }
        appData = (AppData) getItem(position);
        viewDataSetup(appData, viewData);
        return view;
    }

    private void viewDataSetup(AppData appData, ViewData viewData) {
        viewData.appIcon.setImageDrawable(appData.getAppFig());
        viewData.tv_AppName.setText(appData.getAppName());
        viewData.tv_memory.setText(String.valueOf(appData.getMemory()) + "KB");
        viewData.tv_process.setText(appData.getProcess());

        viewData.tv_pid.setText(String.valueOf(appData.getPid()));
        viewData.tv_percent.setText("Battery use  " + String.format("%.2f", appData.getPercent()) + "%");
        viewData.tv_cpu.setText("CPU Time: " + String.format("%.3f", appData.getCpuTime() / 1000) + "s");
    }
}
