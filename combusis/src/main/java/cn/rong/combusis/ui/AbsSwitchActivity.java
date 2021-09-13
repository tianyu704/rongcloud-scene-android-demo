/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rong.combusis.ui;


import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.basis.ui.BaseActivity;

import java.util.Arrays;

import cn.rong.combusis.R;

public class AbsSwitchActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int setLayoutId() {
        return R.layout.activity_abs_switch;
    }

    @Override
    public void init() {
        initView();
    }

    private ViewPager2 viewPager;
    private TextView sw_left, sw_right;
    private int currentIndex = 0;

    protected void initView() {
        viewPager = findViewById(R.id.switch_vpage);
        sw_left = findViewById(R.id.sw_left);
        sw_right = findViewById(R.id.sw_right);

        viewPager.setCurrentItem(currentIndex);
        viewPager.setAdapter(new SampleVPAdapter(this, Arrays.asList(onCreateLeftFragment(), onCreateRightFragment())));
        sw_left.setOnClickListener(this);
        sw_right.setOnClickListener(this);
        getView(R.id.fl_back).setOnClickListener(this);
        String[] titles = onSetSwitchTitle();
        if (null != titles && titles.length == 2) {
            sw_left.setText(titles[0]);
            sw_right.setText(titles[1]);
        }
        getWrapBar().setHide(true).work();

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.sw_left == id) {
            currentIndex = 0;
            refreshSwitchState();
        } else if (R.id.sw_right == id) {
            currentIndex = 1;
            refreshSwitchState();
        } else if (R.id.fl_back == id) {
            onBackCode();
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














