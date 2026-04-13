package com.example.dayclock.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dayclock.data.TaskEntity;
import com.example.dayclock.databinding.FragmentClockBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class ClockFragment extends Fragment {

    private FragmentClockBinding binding;
    private UpcomingTaskAdapter upcomingAdapter;
    private final List<TaskEntity> allTasks = new ArrayList<>();

    private final Handler upcomingHandler = new Handler(Looper.getMainLooper());
    private final Runnable upcomingTicker = new Runnable() {
        @Override
        public void run() {
            if (binding != null) {
                updateUpcomingList();
                upcomingHandler.postDelayed(this, 60_000);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClockBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        upcomingAdapter = new UpcomingTaskAdapter();
        binding.recyclerUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUpcoming.setAdapter(upcomingAdapter);

        TaskViewModel viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks.clear();
            if (tasks != null) {
                allTasks.addAll(tasks);
            }
            binding.clockView.setTasks(allTasks);
            updateUpcomingList();
        });

        upcomingHandler.post(upcomingTicker);
    }

    private void updateUpcomingList() {
        List<TaskEntity> upcoming = buildUpcomingTasks(allTasks, 6);
        upcomingAdapter.setItems(upcoming);
        boolean isEmpty = upcoming.isEmpty();
        binding.tvUpcomingEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private List<TaskEntity> buildUpcomingTasks(List<TaskEntity> source, int limit) {
        List<TaskEntity> result = new ArrayList<>(source);
        int nowMinutes = getNowMinutes();

        result.sort(Comparator
                .comparing((TaskEntity task) -> !isTaskActive(task, nowMinutes))
                .thenComparingInt(task -> minutesUntilStart(task, nowMinutes)));

        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    private int minutesUntilStart(TaskEntity task, int nowMinutes) {
        int delta = task.startMinutes - nowMinutes;
        if (delta < 0) {
            delta += 1440;
        }
        return delta;
    }

    private boolean isTaskActive(TaskEntity task, int nowMinutes) {
        if (task.endMinutes > task.startMinutes) {
            return nowMinutes >= task.startMinutes && nowMinutes < task.endMinutes;
        }
        return nowMinutes >= task.startMinutes || nowMinutes < task.endMinutes;
    }

    private int getNowMinutes() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        upcomingHandler.removeCallbacks(upcomingTicker);
        binding = null;
    }
}
