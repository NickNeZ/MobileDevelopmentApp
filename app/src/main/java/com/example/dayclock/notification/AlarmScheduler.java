package com.example.dayclock.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.dayclock.data.TaskEntity;

import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {

    public static void scheduleTask(Context context, TaskEntity task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra("task_id", task.id);
        intent.putExtra("title", task.title);
        intent.putExtra("startMinutes", task.startMinutes);
        intent.putExtra("endMinutes", task.endMinutes);
        intent.putExtra("color", task.color);
        intent.putExtra("notificationsEnabled", task.notificationsEnabled);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);

        if (!task.notificationsEnabled) {
            return;
        }

        long triggerTime = getNextTriggerTime(task.startMinutes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }

    public static void cancelTask(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    public static void rescheduleAll(Context context, List<TaskEntity> tasks) {
        if (tasks == null) {
            return;
        }
        for (TaskEntity task : tasks) {
            scheduleTask(context, task);
        }
    }

    private static long getNextTriggerTime(int startMinutes) {
        Calendar now = Calendar.getInstance();
        Calendar trigger = Calendar.getInstance();

        trigger.set(Calendar.HOUR_OF_DAY, startMinutes / 60);
        trigger.set(Calendar.MINUTE, startMinutes % 60);
        trigger.set(Calendar.SECOND, 0);
        trigger.set(Calendar.MILLISECOND, 0);

        if (trigger.getTimeInMillis() <= now.getTimeInMillis()) {
            trigger.add(Calendar.DAY_OF_YEAR, 1);
        }

        return trigger.getTimeInMillis();
    }
}
