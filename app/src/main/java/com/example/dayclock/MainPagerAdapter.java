package com.example.dayclock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.dayclock.ui.ClockFragment;
import com.example.dayclock.ui.TasksFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new ClockFragment() : new TasksFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
