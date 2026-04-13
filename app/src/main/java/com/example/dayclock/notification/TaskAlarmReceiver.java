package com.example.dayclock.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dayclock.data.TaskEntity;

public class TaskAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("task_id", 0);
        String title = intent.getStringExtra("title");
        int startMinutes = intent.getIntExtra("startMinutes", 0);
        int endMinutes = intent.getIntExtra("endMinutes", 0);
        int color = intent.getIntExtra("color", 0);
        boolean notificationsEnabled = intent.getBooleanExtra("notificationsEnabled", true);

        String safeTitle = title == null ? "Без названия" : title;

        NotificationUtils.createChannel(context);
        NotificationUtils.showTaskNotification(context, taskId, safeTitle, color);

        TaskEntity task = new TaskEntity(safeTitle, startMinutes, endMinutes, color, notificationsEnabled);
        task.id = taskId;
        AlarmScheduler.scheduleTask(context, task);
    }
}
