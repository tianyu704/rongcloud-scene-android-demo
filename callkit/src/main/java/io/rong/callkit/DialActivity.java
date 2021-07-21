/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */
package io.rong.callkit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bcq.adapter.interfaces.IHolder;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.bcq.net.Request;
import com.bcq.net.api.Method;
import com.bcq.net.net.ListCallback;
import com.bcq.net.wrapper.interfaces.IResult;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.rong.callkit.dialpad.DialInfo;
import io.rong.callkit.dialpad.DialpadFragment;
import io.rong.callkit.dialpad.animation.AnimUtils;
import io.rong.callkit.dialpad.widget.FloatingActionButtonController;
import io.rong.imkit.widget.adapter.BaseAdapter;

/**
 * 拨号界面
 */
public class DialActivity extends AppCompatActivity implements View.OnClickListener, DialpadFragment.DialpadListener {
    @VisibleForTesting
    public static final String TAG = "DialActivity";
    public static final String TAG_DIALPAD_FRAGMENT = "dialpad";
    private FloatingActionButtonController mFloatingActionButtonController;
    private ImageButton floatingActionButton;
    private RecyclerView recyclerView;

    public static void openDilapadPage(Activity activity, boolean video) {
        activity.startActivity(new Intent(activity, DialActivity.class).putExtra(TAG, video));
    }

    @SuppressLint("RestrictedApi")
    protected void initActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setElevation(0f);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_dialer_light)));
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        Log.e(TAG, "titleId = "+titleId);
        TextView tvTitle = findViewById(titleId);
        if (null != tvTitle) {
            tvTitle.setGravity(Gravity.CENTER);
            Log.e(TAG, "get title");
        }else {
            Log.e(TAG, "not get title");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialtacts_activity);
        boolean video = getIntent().getBooleanExtra(TAG, false);
        String title = video ? "视频通话" : "语音通话";
        initActionBar(title);
        initView();
    }

    private void initView() {
        final View floatingActionButtonContainer = findViewById(
                R.id.floating_action_button_container);
        floatingActionButton = (ImageButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(this);
        mFloatingActionButtonController = new FloatingActionButtonController(this,
                floatingActionButtonContainer, floatingActionButton);
        recyclerView = findViewById(R.id.rc_refresh);
        recyclerView.setAdapter(new RcySAdapter<DialInfo, RcyHolder>(this, R.layout.layout_dialpad_item) {

            @Override
            public void convert(RcyHolder holder, DialInfo dialInfo, int position) {
                holder.setText(R.id.tv_number, dialInfo.getPhone());
                holder.setText(R.id.tv_date, String.valueOf(dialInfo.getDate()));
            }
        });
        ArrayList<DialInfo> infoList = new ArrayList<>();
        DialInfo temp;
        for (int i = 0; i < 20; i++) {
            temp = new DialInfo();
            temp.setDate(new Date().getTime());
            temp.setPhone(i < 10 ? "1851037150" : "185103715" + i);
            infoList.add(temp);
        }
        ((RcySAdapter) recyclerView.getAdapter()).setData(infoList, true);
    }

    boolean mIsDialpadShown;

    @Override
    public void onClick(View view) {
        int resId = view.getId();
        if (resId == R.id.floating_action_button) {
            if (!mIsDialpadShown) {
                showDialpadFragment();
            }
        }
    }

    @Override
    public void onDialpad(String num) {
        Log.e(TAG, "onDialpad:num = " + num);
        getUserIdByPhone(num);
    }

    @Override
    public void onInputFiltter(Editable input) {
        Log.e(TAG, "onInputChanage:input = " + input);
    }

    private void getUserIdByPhone(String phone){
        String url = "http://120.92.102.127:8081/user/get/"+phone;
        Request.request( null, url, null, Method.get, new ListCallback<String>(String.class) {
            @Override
            public void onResult(IResult.ObjResult<List<String>> result) {
                super.onResult(result);
            }
        });
    }

    DialpadFragment dialpadFragment;

    private void showDialpadFragment() {
        if (mIsDialpadShown) {
            return;
        }
        mIsDialpadShown = true;
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (dialpadFragment == null) {
            dialpadFragment = new DialpadFragment();
            DialpadFragment.dialpadListener = new WeakReference<DialpadFragment.DialpadListener>(DialActivity.this);
            ft.add(R.id.dialtacts_container, dialpadFragment, TAG_DIALPAD_FRAGMENT);
        } else {
            ft.show(dialpadFragment);
        }
        ft.commitAllowingStateLoss();
        mFloatingActionButtonController.scaleOut();
        floatingActionButton.setImageResource(R.drawable.fab_ic_call);
    }

    public void hideDialpadFragment(boolean clearDialpad) {
        if (dialpadFragment == null || dialpadFragment.getView() == null) {
            return;
        }
        if (clearDialpad) {
            // Temporarily disable accessibility when we clear the dialpad, since it should be
            // invisible and should not announce anything.
            dialpadFragment.getDigitsWidget().setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            dialpadFragment.clearDialpad();
            dialpadFragment.getDigitsWidget().setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
        if (!mIsDialpadShown) {
            return;
        }
        mIsDialpadShown = false;
        commitDialpadFragmentHide();
        floatingActionButton.setImageResource(R.drawable.fab_ic_dial);
    }

    private void commitDialpadFragmentHide() {
        if (dialpadFragment != null && !dialpadFragment.isHidden()) {
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(dialpadFragment);
            ft.commit();
        }
        mFloatingActionButtonController.scaleIn(AnimUtils.NO_DELAY);
    }

}
