package com.example.dayclock.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayclock.R;
import com.example.dayclock.data.TaskEntity;
import com.example.dayclock.databinding.ItemUpcomingTaskBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpcomingTaskAdapter extends RecyclerView.Adapter<UpcomingTaskAdapter.UpcomingTaskViewHolder> {

    private final List<TaskEntity> items = new ArrayList<>();

    public void setItems(List<TaskEntity> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UpcomingTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUpcomingTaskBinding binding = ItemUpcomingTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UpcomingTaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingTaskViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class UpcomingTaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemUpcomingTaskBinding binding;

        UpcomingTaskViewHolder(ItemUpcomingTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TaskEntity task) {
            binding.tvTitle.setText(task.title);
            binding.tvTime.setText(formatTime(task.startMinutes) + " - " + formatTime(task.endMinutes));
            binding.tvMeta.setText(getRelativeText(task));

            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setShape(GradientDrawable.OVAL);
            colorDrawable.setColor(task.color);
            binding.colorDot.setBackground(colorDrawable);

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
            int now = getNowMinutes();
            if (task.endMinutes > task.startMinutes) {
                return now >= task.startMinutes && now < task.endMinutes;
            }
            return now >= task.startMinutes || now < task.endMinutes;
        }

        private String getRelativeText(TaskEntity task) {
            if (isTaskActive(task)) {
                return "Сейчас выполняется";
            }

            int now = getNowMinutes();
            int delta = task.startMinutes - now;
            if (delta < 0) {
                delta += 1440;
            }

            int hours = delta / 60;
            int mins = delta % 60;

            if (hours > 0) {
                return String.format(Locale.getDefault(), "Через %d ч %d мин", hours, mins);
            }
            return String.format(Locale.getDefault(), "Через %d мин", mins);
        }

        private int getNowMinutes() {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        }

        private int dpToPx(int dp) {
            float density = binding.getRoot().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
