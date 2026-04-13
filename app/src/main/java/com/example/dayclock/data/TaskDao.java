package com.example.dayclock.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY startMinutes ASC")
    LiveData<java.util.List<TaskEntity>> getAll();

    @Query("SELECT * FROM tasks ORDER BY startMinutes ASC")
    List<TaskEntity> getAllNow();

    @Insert
    long insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);
}
