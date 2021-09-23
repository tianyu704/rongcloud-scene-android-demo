/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom2

import android.os.Bundle
import cn.rongcloud.voiceroomdemo.R
import com.rongcloud.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val TAG = "VoiceRoomFragment"
private const val KEY_ROOM_ID = "KEY_ROOM_INFO_BEAN"
private const val KEY_CREATOR_ID = "KEY_CREATOR_ID"
private const val KEY_IS_CREATE = "KEY_IS_CREATE"

@AndroidEntryPoint
class BGFragment : BaseFragment(R.layout.fragment_bg) {

    companion object {
        fun newInstance(
            roomId: String,
            createUserId: String,
            isCreate: Boolean = false
        ): BGFragment {
            return BGFragment().apply {
                this.arguments = Bundle().apply {
                    putString(KEY_ROOM_ID, roomId)
                    putString(KEY_CREATOR_ID, createUserId)
                    putBoolean(KEY_IS_CREATE, isCreate)
                }
            }
        }
    }

    @Inject
    lateinit var presenter: VoiceRoomFragmentPresenter
    override fun initView() {
        showWaitingDialog()
    }

}












