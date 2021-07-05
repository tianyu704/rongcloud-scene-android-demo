/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.*
import com.example.voiceroomdemo.mvp.activity.iview.IHomeView
import com.example.voiceroomdemo.mvp.presenter.HomePresenter
import com.example.voiceroomdemo.ui.dialog.UserInfoDialog
import com.example.voiceroomdemo.utils.LocalUserInfoManager
import de.hdodenhof.circleimageview.CircleImageView
import io.rong.imkit.utils.RouteUtils
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_portrait.*

private const val PICTURE_SELECTED_RESULT_CODE = 10001

private const val TAG = "HomeActivity"


class HomeActivity : BaseActivity<HomePresenter, IHomeView>(), IHomeView {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }
    }


    private var userInfoDialog: UserInfoDialog? = null

    override fun initPresenter(): HomePresenter = HomePresenter(this, this)

    override fun getContentView(): Int = R.layout.activity_home


    override fun initView() {
        iv_voice_room.setOnClickListener {
            VoiceRoomListActivity.startActivity(this)
        }

        iv_video_call.setOnClickListener {
            showToast("暂未实现")
        }

        iv_audio_call.setOnClickListener {
            showToast("暂未实现")
        }
    }

    override fun getActionTitle(): CharSequence? {
        return null
    }

    override fun getLeftActionButton(): View? {
        val portrait =
            LayoutInflater.from(this).inflate(R.layout.layout_portrait, null) as CircleImageView
        portrait.setOnClickListener {
            userInfoDialog = UserInfoDialog(this, {
                // 退出登录
                presenter.logout()
            }, { userName, selectedPicPath ->
                // 修改用户名和头像
                presenter.modifyUserInfo(userName, selectedPicPath)
            }, {
                // 进入头像选择界面
                startPicSelectActivity()
            })
            userInfoDialog?.show()
        }
        portrait.loadPortrait(AccountStore.getUserPortrait() ?: "")
        return portrait
    }

    override fun onDestroy() {
        super.onDestroy()
        userInfoDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    override fun getRightActionButton(): View? {
        val imageView = LayoutInflater.from(this)
            .inflate(R.layout.layout_right_title_icon, null) as AppCompatImageView
        imageView.setImageResource(R.drawable.ic_message)
        imageView.setOnClickListener {
            RouteUtils.routeToSubConversationListActivity(
                this,
                Conversation.ConversationType.PRIVATE,
                "消息"
            )
        }
        return imageView
    }

    override fun initData() {
        LocalUserInfoManager.getUserInfoByUserId("");
    }

    override fun modifyInfoSuccess() {
        ui {
            userInfoDialog?.dismiss()
            iv_portrait.loadPortrait(AccountStore.getUserPortrait() ?: "")

        }
    }

    override fun showNormal() {
    }


    private fun startPicSelectActivity() {
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICTURE_SELECTED_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_SELECTED_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val selectImageUrl = data?.data;
            val filePathColumn = arrayOf<String>(MediaStore.Images.Media.DATA)
            // 查询我们需要的数据
            selectImageUrl?.let {
                val cursor: Cursor? = contentResolver.query(
                    selectImageUrl,
                    filePathColumn, null, null, null
                )
                cursor?.moveToFirst()

                val columnIndex: Int = cursor?.getColumnIndex(filePathColumn[0])!!
                val picturePath: String = cursor.getString(columnIndex)
                cursor.close()
                userInfoDialog?.setUserPortrait(picturePath)
            }
        }
    }

}