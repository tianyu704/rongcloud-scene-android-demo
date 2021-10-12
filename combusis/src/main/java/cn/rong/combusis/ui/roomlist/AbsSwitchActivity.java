/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rong.combusis.ui.roomlist;


import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.basis.ui.BaseActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;

import cn.rong.combusis.R;
import io.rong.imkit.utils.StatusBarUtil;

public class AbsSwitchActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int setLayoutId() {
        return R.layout.activity_abs_switch;
    }

    @Override
    public void init() {
        initView();
    }

    private ViewPager vp_switch;
    private TabLayout tab_switch;
    private int currentIndex = 0;

    protected void initView() {
        StatusBarUtil.setStatusBarFontIconDark(this, StatusBarUtil.TYPE_M, true);
        vp_switch = findViewById(R.id.vp_switch);

        tab_switch = findViewById(R.id.tab_switch);

        vp_switch.setCurrentItem(currentIndex);
        vp_switch.setAdapter(new VPAdapter(getSupportFragmentManager(), Arrays.asList(onCreateLeftFragment(), onCreateRightFragment()), onSetSwitchTitle()));

        tab_switch.setupWithViewPager(vp_switch);
//        tab_switch.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                TextView textView = new TextView(AbsSwitchActivity.this);
//                textView.setText(tab.getText());
//                textView.setTextSize(19);
//                tab.setCustomView(textView);
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                tab.setCustomView(null);
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });

        getView(R.id.fl_back).setOnClickListener(this);

        getWrapBar().setHide(true).work();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.fl_back == id) {
            onBackCode();
        }
    }

    public String[] onSetSwitchTitle() {
        return new String[]{"房间", "好友"};
    }

    public Fragment onCreateLeftFragment() {
        return null;
    }

    public Fragment onCreateRightFragment() {
        return null;
    }

}














