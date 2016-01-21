package com.hd.cu.android_project2;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Welcome extends Activity {

    private ActivityManager activityManager = null;
    private TextView avaMem, battery;
    private Button runningApp;
    private BroadcastReceiver batteryLevel;
    private IntentFilter batteryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        avaMem = (TextView) findViewById(R.id.tv_avaMemory);
        battery = (TextView) findViewById(R.id.tv_battery_level);
        runningApp = (Button) findViewById(R.id.btn_RunningApp);
        runningApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, Applist.class);
                startActivity(intent);
            }
        });

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        avaMem.setText("The Available Memory：" + getSystemAvaialbeMemorySize());
        batteryState();
    }

    private void batteryState() {
        batteryLevel = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                StringBuilder stringBuilder = new StringBuilder();
                int batteryPer = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int status = intent.getIntExtra("status", -1);
                int health = intent.getIntExtra("health", -1);
                int percent = (batteryPer * 100) / scale;
                stringBuilder.append("Battery: ");
                stringBuilder.append(percent + "%");
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        stringBuilder.append("[No Battery!]");
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        stringBuilder.append("[Charging...]");
                        break;
                    default:
                        if (percent <= 20)
                            stringBuilder.append("[Battery Low!!]");
                        else if (percent <= 100) {
                            stringBuilder.append("[Unconnected with charger]");
                        }
                        break;

                }
                battery.setText(stringBuilder.toString());
            }
        };
        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevel, batteryFilter);

    }

    private String getSystemAvaialbeMemorySize() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return Formatter.formatFileSize(Welcome.this, memoryInfo.availMem);
    }

}
