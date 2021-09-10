/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rong.combusis.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.rongcloud.common.base.PermissionActivity;

import java.util.Arrays;

import cn.rong.combusis.R;

public class AbsSwitchActivity extends PermissionActivity implements View.OnClickListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abs_switch);
    }

    @Override
    protected void onAccept(@NonNull boolean accept) {
        if (accept) {
            initView();
        }
    }

    @Nullable
    @Override
    protected String[] onSetPermissions() {
        return new String[0];
    }

    private ViewPager2 viewPager;
    private TextView sw_left, sw_right;
    private int currentIndex = 0;

    protected void initView() {
        viewPager = findViewById(R.id.switch_vpage);
        sw_left = findViewById(R.id.sw_left);
        sw_right = findViewById(R.id.sw_right);
        viewPager.setCurrentItem(currentIndex);
        viewPager.setAdapter(new SampleVPAdapter(this, Arrays.asList(onCreateLeftFragment(), onCreateLeftFragment())));
        sw_left.setOnClickListener(this);
        String[] titles = onSetSwitchTitle();
        if (null != titles && titles.length == 2) {
            sw_left.setText(titles[0]);
            sw_right.setText(titles[1]);
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.sw_left == id) {
            currentIndex = 0;
            refreshSwitchState();
        } else if (R.id.sw_left == id) {
            currentIndex = 1;
            refreshSwitchState();
        }

    }

    private void refreshSwitchState() {
        viewPager.setCurrentItem(currentIndex);
        sw_left.setTextSize(currentIndex == 0 ? 18 : 14);
        sw_right.setTextSize(currentIndex == 0 ? 14 : 18);
    }

    String[] onSetSwitchTitle() {
        return new String[]{"语聊房", "好友"};
    }

    public Fragment onCreateLeftFragment() {
        return null;
    }


    public Fragment onCreateRightFragment() {
        return null;
    }


}














