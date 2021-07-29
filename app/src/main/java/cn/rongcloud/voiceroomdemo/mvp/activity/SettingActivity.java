/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.activity;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.ImageLoaderUtil;

import cn.rongcloud.voiceroomdemo.R;
import cn.rongcloud.voiceroomdemo.ui.dialog.UserInfoDialog;
import cn.rongcloud.voiceroomdemo.webview.ActCommentWeb;
import cn.rongcloud.voiceroomdemo.webview.BaseActionBarActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;


public class SettingActivity extends BaseActionBarActivity implements View.OnClickListener {
    UserInfoDialog dialog;

    public static void startActivity(Activity activity) {
        activity.startActivity(new Intent(activity, SettingActivity.class));
    }

    @Override
    public int getContentView() {
        return R.layout.activity_setting;
    }

    private ImageView iv_portrait;
    private TextView tv_name;

    @Override
    public void initView() {
        iv_portrait = findViewById(R.id.iv_portrait);
        tv_name = findViewById(R.id.tv_name);
        iv_portrait.setOnClickListener(this);
        findViewById(R.id.ad_first).setOnClickListener(this);
        findViewById(R.id.ad_second).setOnClickListener(this);
        findViewById(R.id.ad_third).setOnClickListener(this);
        findViewById(R.id.ad_fourth).setOnClickListener(this);
        findViewById(R.id.ad_fivth).setOnClickListener(this);
        findViewById(R.id.customer_dial).setOnClickListener(this);
        initDefalutActionBar("");
    }

    @Override
    public void initData() {
        tv_name.setText(AccountStore.INSTANCE.getUserName());
        String url = AccountStore.INSTANCE.getUserPortrait();
        if (TextUtils.isEmpty(url)) {
            iv_portrait.setImageResource(R.drawable.default_portrait);
        } else {
            ImageLoaderUtil.INSTANCE.loadPortrait(SettingActivity.this, iv_portrait, url);
        }
    }

    /**
     * 以绿色版本为准：
     * Banner 跳转
     * https://m.rongcloud.cn/activity/rtc20
     * 套餐方案：
     * https://m.rongcloud.cn/activity/rtc20
     * Demo 下载：
     * https://m.rongcloud.cn/downloads/demo
     * 在线客服：
     * https://m.rongcloud.cn/cs
     * 关于我们：
     * https://m.rongcloud.cn/about
     * 专属客户经理：
     * （系统拨打电话 ActionSheet） 13161856839
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_portrait:
                showEditeInfoDialog();
                break;
            case R.id.ad_first:
                ActCommentWeb.openCommentWeb(this, "https://m.rongcloud.cn/activity/rtc20", "套餐方案");
                break;
            case R.id.ad_second:
                ActCommentWeb.openCommentWeb(this, "https://m.rongcloud.cn/activity/rtc20", "套餐方案");
                break;
            case R.id.ad_third:
                ActCommentWeb.openCommentWeb(this, "https://m.rongcloud.cn/downloads/demo", "Demo 下载");
                break;
            case R.id.ad_fourth:
                ActCommentWeb.openCommentWeb(this, "https://m.rongcloud.cn/cs", "在线客服");
                break;
            case R.id.ad_fivth:
                ActCommentWeb.openCommentWeb(this, "https://m.rongcloud.cn/about", "关于我们");
                break;
            case R.id.customer_dial:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:13161856839");
                intent.setData(data);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10000 && resultCode == Activity.RESULT_OK) {
            Uri selectImageUrl = data.getData();
            if (null != selectImageUrl && null != dialog) {
                dialog.setUserPortrait(selectImageUrl);
            }
        }
    }

    private void showEditeInfoDialog() {
        dialog = new UserInfoDialog(this, new Function0() {
            @Override
            public Object invoke() {
                AccountStore.INSTANCE.logout();
                return null;
            }
        }, new Function2<String, Uri, Unit>() {
            @Override
            public Unit invoke(String s, Uri uri) {
//                        .modifyUserInfo(userName, selectedPicPath);
                // TODO: 2021/7/29 修改名称
                return null;
            }
        }, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 10000);
                return null;
            }
        });
        dialog.show();
    }
}