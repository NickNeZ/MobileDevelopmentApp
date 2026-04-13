package com.example.dayclock.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "tasks")
public class TaskEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public int startMinutes;
    public int endMinutes;
    public int color;
    public boolean notificationsEnabled;

    @Ignore
    public TaskEntity(String title, int startMinutes, int endMinutes, int color) {
        this(title, startMinutes, endMinutes, color, true);
    }

    public TaskEntity(String title, int startMinutes, int endMinutes, int color, boolean notificationsEnabled) {
        this.title = title;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.color = color;
        this.notificationsEnabled = notificationsEnabled;
    }
}
