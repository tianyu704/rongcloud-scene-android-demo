/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */
package io.rong.callkit;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;

import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rongcloud.common.dao.database.DatabaseManager;
import com.rongcloud.common.dao.entities.CallRecordEntityKt;
import com.rongcloud.common.dao.model.query.CallRecordModel;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.rong.callkit.dialpad.DialInfo;
import io.rong.callkit.dialpad.DialpadFragment;
import io.rong.callkit.dialpad.animation.AnimUtils;
import io.rong.callkit.dialpad.widget.FloatingActionButtonController;
import io.rong.callkit.util.DateUtil;
import io.rong.callkit.util.GlideUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.rongcloud.common.dao.entities.CallRecordEntityKt.AUDIO_SINGLE_CALL;

/**
 * 拨号界面
 */
public class DialActivity extends BaseActionBarActivity implements View.OnClickListener, DialpadFragment.DialpadListener {
    @VisibleForTesting
    public static final String TAG = "DialActivity";
    public static final String KEY_VIDEO = "is_video";
    public static final String KEY_ID = "user_id";
    public static final String KEY_TOKEN = "token";
    public static final String TAG_DIALPAD_FRAGMENT = "dialpad";
    private FloatingActionButtonController mFloatingActionButtonController;
    private ImageButton floatingActionButton;
    private RecyclerView recyclerView;
    private boolean isVideo = false;
    private String userId, token;
    private List<DialInfo> records = new ArrayList<>();

    public static void openDilapadPage(Activity activity, String userId, boolean video, String token) {
        activity.startActivity(new Intent(activity, DialActivity.class)
                .putExtra(KEY_VIDEO, video)
                .putExtra(KEY_ID, userId)
                .putExtra(KEY_TOKEN, token));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialtacts_activity);
        isVideo = getIntent().getBooleanExtra(KEY_VIDEO, false);
        userId = getIntent().getStringExtra(KEY_ID);
        token = getIntent().getStringExtra(KEY_TOKEN);
        String title = isVideo ? "视频通话" : "语音通话";
        initDefalutActionBar(title);
        initView();
        DatabaseManager.INSTANCE.obCallRecordList(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CallRecordModel>>() {
                    @Override
                    public void accept(List<CallRecordModel> models) {
                        int size = null == models ? 0 : models.size();
                        DialInfo dialInfo;
                        for (int i = 0; i < size; i++) {
                            CallRecordModel model = models.get(i);
                            dialInfo = new DialInfo();
                            dialInfo.setPhone(model.getPeerNumber());
                            dialInfo.setUserId(model.getPeerId());
                            dialInfo.setDate(model.getDate());
                            dialInfo.setHead(model.getPortrait());
                            records.add(dialInfo);
                        }
                        refreshRecords();
                    }
                });
    }

    private void refreshRecords() {
        if (null != records && null != recyclerView.getAdapter()) {
            ((RcySAdapter) recyclerView.getAdapter()).setData(records, true);
        }
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
            public void convert(RcyHolder holder, DialInfo info, int position) {
                holder.setText(R.id.tv_number, info.getPhone());
                holder.setText(R.id.tv_date, DateUtil.getRecordDate(info.getDate()));
                holder.setText(R.id.tv_date, DateUtil.getRecordDate(info.getDate()));
                ImageView head = holder.getView(R.id.iv_head);
                GlideUtils.showBlurTransformation(
                        context,
                        head,
                        Uri.parse(info.getHead()));
            }
        });
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
        getUserIdByPhone(num);
    }

    @Override
    public void onInputFiltter(Editable input) {
//        Log.e(TAG, "onInputChanage:input = " + input);
        if (null != records && !records.isEmpty()) {


        }
    }

    private void getUserIdByPhone(final String phone) {
        String url = "http://120.92.102.127:8081/user/get/" + phone;
        Log.e(TAG, "getUserIdByPhone:url = " + url);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e(TAG, "onResponse: " + result);
                JsonObject jsonObj = JsonParser.parseString(result).getAsJsonObject();
                if (null != jsonObj) {
                    int code = jsonObj.get("code").getAsInt();
                    if (code == 10000) {
                        JsonObject data = jsonObj.get("data").getAsJsonObject();
                        String userId = data.get("uid").getAsString();
                        DatabaseManager.INSTANCE.insertCallRecord(
                                userId,
                                "",
                                userId,
                                phone,
                                new Date().getTime(),
                                0,
                                isVideo ? CallRecordEntityKt.VIDEO_SINGLE_CALL : CallRecordEntityKt.AUDIO_SINGLE_CALL
                        );
                        RongCallKit.startSingleCall(DialActivity.this, userId,
                                isVideo ? RongCallKit.CallMediaType.CALL_MEDIA_TYPE_VIDEO
                                        : RongCallKit.CallMediaType.CALL_MEDIA_TYPE_AUDIO);
                    }
                }

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
