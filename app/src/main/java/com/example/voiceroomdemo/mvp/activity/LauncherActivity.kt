/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.activity

import android.app.Application
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback
import com.example.voiceroomdemo.MyApp
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.setAndroidNativeLightStatusBar
import com.example.voiceroomdemo.common.showToast
import kotlinx.android.synthetic.main.activity_launcher.*
import java.util.*


class LauncherActivity : PermissionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAndroidNativeLightStatusBar(true)
        setContentView(R.layout.activity_launcher)
    }

    override fun onSetPermissions(): Array<String> {
        return PERMISSIONS;
    }

    override fun onAccept(accept: Boolean) {
        Log.e("LauncherActivity","accept = "+accept)
        if (accept) {
            turnToActivity()
        } else {
            showToast("请赋予必要权限！")
            finish()
        }
    }

    private fun turnToActivity() {
        if (AccountStore.getImToken().isNullOrBlank()) {
            LoginActivity.startActivity(this)
            finish()
        } else {
            RCVoiceRoomEngine.getInstance().connectWithToken(
                MyApp.context as Application,
                AccountStore.getImToken(),
                object : RCVoiceRoomCallback {
                    override fun onError(code: Int, message: String?) {
                        LoginActivity.startActivity(this@LauncherActivity)
                        finish()
                    }

                    override fun onSuccess() {
                        HomeActivity.startActivity(this@LauncherActivity)
                        finish()
                    }

                })
        }
    }
}