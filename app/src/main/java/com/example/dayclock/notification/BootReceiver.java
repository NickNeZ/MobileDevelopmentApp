package com.example.dayclock.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dayclock.data.AppDatabase;
import com.example.dayclock.data.TaskEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<TaskEntity> tasks = AppDatabase.getInstance(context).taskDao().getAllNow();
            AlarmScheduler.rescheduleAll(context, tasks);
        });
    }
}
