package com.example.dayclock.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayclock.R;
import com.example.dayclock.data.TaskEntity;
import com.example.dayclock.databinding.ItemTaskBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskActionListener {
        void onEdit(TaskEntity task);
        void onDelete(TaskEntity task);
    }

    private final TaskActionListener listener;
    private final List<TaskEntity> items = new ArrayList<>();

    public TaskAdapter(TaskActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TaskEntity> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    public void refreshActiveState() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemTaskBinding binding;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TaskEntity task) {
            binding.tvTitle.setText(task.title);
            binding.tvTime.setText(formatTime(task.startMinutes) + " - " + formatTime(task.endMinutes));

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(task.color);
            binding.colorDot.setBackground(drawable);

            binding.btnEdit.setOnClickListener(v -> listener.onEdit(task));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(task));

            MaterialCardView cardView = binding.getRoot();
            if (isTaskActive(task)) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), R.color.active_task_background));
                cardView.setStrokeColor(task.color);
                cardView.setStrokeWidth(dpToPx(2));
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), android.R.color.white));
                cardView.setStrokeColor(ContextCompat.getColor(cardView.getContext(), android.R.color.transparent));
                cardView.setStrokeWidth(0);
            }
        }

        private String formatTime(int minutes) {
            int h = minutes / 60;
            int m = minutes % 60;
            return String.format(Locale.getDefault(), "%02d:%02d", h, m);
        }

        private boolean isTaskActive(TaskEntity task) {
            Calendar calendar = Calendar.getInstance();
            int now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            if (task.endMinutes > task.startMinutes) {
                return now >= task.startMinutes && now < task.endMinutes;
            }
            return now >= task.startMinutes || now < task.endMinutes;
        }

        private int dpToPx(int dp) {
            float density = binding.getRoot().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
