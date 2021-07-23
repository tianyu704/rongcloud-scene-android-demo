/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.activity

import android.os.Bundle
import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.common.AccountStore
import cn.rongcloud.voiceroomdemo.common.setAndroidNativeLightStatusBar
import cn.rongcloud.voiceroomdemo.common.showToast
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
            HomeActivity.startActivity(this)
            finish()
        }
    }
}