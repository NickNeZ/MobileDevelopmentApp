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
import com.example.dayclock.databinding.FragmentTasksBinding;

public class TasksFragment extends Fragment implements TaskAdapter.TaskActionListener {

    private FragmentTasksBinding binding;
    private TaskAdapter adapter;
    private TaskViewModel viewModel;
    private final Handler activeTaskHandler = new Handler(Looper.getMainLooper());
    private final Runnable activeTaskTicker = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.refreshActiveState();
                activeTaskHandler.postDelayed(this, 60_000);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        adapter = new TaskAdapter(this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> adapter.setItems(tasks));
        activeTaskHandler.post(activeTaskTicker);

        binding.fabAdd.setOnClickListener(v ->
                TaskDialogFragment.newInstance(null)
                        .show(getParentFragmentManager(), "add_task")
        );
    }

    @Override
    public void onEdit(TaskEntity task) {
        TaskDialogFragment.newInstance(task)
                .show(getParentFragmentManager(), "edit_task");
    }

    @Override
    public void onDelete(TaskEntity task) {
        viewModel.delete(task);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activeTaskHandler.removeCallbacks(activeTaskTicker);
        binding = null;
    }
}
