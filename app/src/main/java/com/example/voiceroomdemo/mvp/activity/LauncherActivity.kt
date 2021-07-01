/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.activity

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback
import com.example.voiceroomdemo.MyApp
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.setAndroidNativeLightStatusBar
import kotlinx.android.synthetic.main.activity_launcher.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

private const val PERMISSION_STORAGE_CODE = 1001;
private val PERMISSIONS = arrayOf(
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    android.Manifest.permission.RECORD_AUDIO
)

class LauncherActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAndroidNativeLightStatusBar(true)
        setContentView(R.layout.activity_launcher)
        if (EasyPermissions.hasPermissions(this, *PERMISSIONS)) {
            iv_logo.postDelayed({
                turnToActivity()
            }, 1000L)
        } else {
            EasyPermissions.requestPermissions(this, "", PERMISSION_STORAGE_CODE, *PERMISSIONS)
        }
    }

    @AfterPermissionGranted(PERMISSION_STORAGE_CODE)
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }


}