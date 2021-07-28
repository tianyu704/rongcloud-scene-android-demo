/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.activity

import android.app.Application
import android.os.Bundle
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback
import cn.rongcloud.voiceroomdemo.MyApp
import cn.rongcloud.voiceroomdemo.R
import com.rongcloud.common.extension.showToast
import com.rongcloud.common.base.PermissionActivity
import com.rongcloud.common.extension.setAndroidNativeLightStatusBar
import com.rongcloud.common.utils.AccountStore
import kotlinx.android.synthetic.main.activity_launcher.*
import java.util.*


class LauncherActivity : PermissionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAndroidNativeLightStatusBar(true)
        setContentView(R.layout.activity_launcher)
    }

    override fun onSetPermissions(): Array<String> {
        return PERMISSIONS
    }

    override fun onAccept(accept: Boolean) {
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