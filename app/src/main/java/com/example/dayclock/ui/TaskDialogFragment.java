package com.example.dayclock.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dayclock.data.TaskEntity;
import com.example.dayclock.databinding.DialogTaskBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class TaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK = "arg_task";

    private DialogTaskBinding binding;
    private TaskEntity editingTask;
    private TaskViewModel viewModel;

    private int selectedStartMinutes = 8 * 60;
    private int selectedEndMinutes = 9 * 60;

    private final int[] colorValues = {
            Color.parseColor("#F44336"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#795548")
    };

    private final String[] colorNames = {
            "Красный", "Оранжевый", "Жёлтый", "Зелёный", "Синий", "Фиолетовый", "Коричневый"
    };

    public static TaskDialogFragment newInstance(@Nullable TaskEntity task) {
        TaskDialogFragment fragment = new TaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogTaskBinding.inflate(LayoutInflater.from(requireContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        if (getArguments() != null) {
            editingTask = (TaskEntity) getArguments().getSerializable(ARG_TASK);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                colorNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerColor.setAdapter(adapter);

        if (editingTask != null) {
            binding.etTitle.setText(editingTask.title);
            selectedStartMinutes = editingTask.startMinutes;
            selectedEndMinutes = editingTask.endMinutes;
            binding.btnStart.setText(formatTime(selectedStartMinutes));
            binding.btnEnd.setText(formatTime(selectedEndMinutes));
            binding.switchNotifications.setChecked(editingTask.notificationsEnabled);

            for (int i = 0; i < colorValues.length; i++) {
                if (colorValues[i] == editingTask.color) {
                    binding.spinnerColor.setSelection(i);
                    break;
                }
            }
        } else {
            binding.btnStart.setText(formatTime(selectedStartMinutes));
            binding.btnEnd.setText(formatTime(selectedEndMinutes));
            binding.switchNotifications.setChecked(true);
        }

        binding.btnStart.setOnClickListener(v -> openTimePicker(true));
        binding.btnEnd.setOnClickListener(v -> openTimePicker(false));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editingTask == null ? "Новая задача" : "Редактировать задачу")
                .setView(binding.getRoot())
                .setNegativeButton("Отмена", (d, which) -> dismiss())
                .setPositiveButton("Сохранить", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> saveTask(dialog)));

        return dialog;
    }

    private void openTimePicker(boolean isStart) {
        int sourceMinutes = isStart ? selectedStartMinutes : selectedEndMinutes;
        int hour = sourceMinutes / 60;
        int minute = sourceMinutes % 60;

        new TimePickerDialog(requireContext(), (view, hourOfDay, minuteValue) -> {
            int result = hourOfDay * 60 + minuteValue;
            if (isStart) {
                selectedStartMinutes = result;
                binding.btnStart.setText(formatTime(result));
            } else {
                selectedEndMinutes = result;
                binding.btnEnd.setText(formatTime(result));
            }
        }, hour, minute, true).show();
    }

    private void saveTask(androidx.appcompat.app.AlertDialog dialog) {
        String title = binding.etTitle.getText() == null ? "" : binding.etTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.etTitle.setError("Введите название");
            return;
        }

        if (selectedStartMinutes == selectedEndMinutes) {
            Toast.makeText(requireContext(), "Начало и конец не должны совпадать", Toast.LENGTH_SHORT).show();
            return;
        }

        int color = colorValues[binding.spinnerColor.getSelectedItemPosition()];
        boolean notificationsEnabled = binding.switchNotifications.isChecked();

        if (editingTask == null) {
            TaskEntity task = new TaskEntity(title, selectedStartMinutes, selectedEndMinutes, color, notificationsEnabled);
            viewModel.insert(task);
        } else {
            editingTask.title = title;
            editingTask.startMinutes = selectedStartMinutes;
            editingTask.endMinutes = selectedEndMinutes;
            editingTask.color = color;
            editingTask.notificationsEnabled = notificationsEnabled;
            viewModel.update(editingTask);
        }

        dialog.dismiss();
    }

    private String formatTime(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }
}
