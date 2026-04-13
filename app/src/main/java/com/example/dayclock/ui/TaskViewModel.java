package com.example.dayclock.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.dayclock.data.TaskEntity;
import com.example.dayclock.data.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<TaskEntity>> allTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    public void insert(TaskEntity task) {
        repository.insert(task);
    }

    public void update(TaskEntity task) {
        repository.update(task);
    }

    public void delete(TaskEntity task) {
        repository.delete(task);
    }

    public void rescheduleAll() {
        repository.rescheduleAll();
    }
}
