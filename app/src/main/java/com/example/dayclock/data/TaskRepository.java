package com.example.dayclock.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.dayclock.notification.AlarmScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final LiveData<List<TaskEntity>> allTasks;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Context context;

    public TaskRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        taskDao = db.taskDao();
        allTasks = taskDao.getAll();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    public void insert(TaskEntity task) {
        executor.execute(() -> {
            long id = taskDao.insert(task);
            task.id = (int) id;
            AlarmScheduler.scheduleTask(context, task);
        });
    }

    public void update(TaskEntity task) {
        executor.execute(() -> {
            taskDao.update(task);
            AlarmScheduler.scheduleTask(context, task);
        });
    }

    public void delete(TaskEntity task) {
        executor.execute(() -> {
            AlarmScheduler.cancelTask(context, task.id);
            taskDao.delete(task);
        });
    }

    public void rescheduleAll() {
        executor.execute(() -> {
            List<TaskEntity> tasks = taskDao.getAllNow();
            AlarmScheduler.rescheduleAll(context, tasks);
        });
    }
}
