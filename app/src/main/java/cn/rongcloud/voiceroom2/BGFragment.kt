/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import cn.rong.combusis.feedback.FeedbackHelper
import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo
import cn.rongcloud.voiceroom.ui.popup.ExitRoomPopupWindow
import cn.rongcloud.voiceroom.ui.uimodel.UiMemberModel
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel
import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.mvp.adapter.VoiceRoomMessageAdapter
import cn.rongcloud.voiceroomdemo.mvp.adapter.VoiceRoomSeatsAdapter
import cn.rongcloud.voiceroomdemo.mvp.fragment.present.ISendPresentView
import cn.rongcloud.voiceroomdemo.mvp.fragment.present.SendPresentFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.present.like.FavAnimation
import cn.rongcloud.voiceroomdemo.mvp.fragment.present.pop.CustomerPopupWindow
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting.CreatorSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting.ICreatorView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting.EmptySeatFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting.IEmptySeatView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.memberlist.IMemberListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.memberlist.MemberListFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.membersetting.IMemberSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.membersetting.MemberSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.IMusicSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.MusicSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest.IRevokeSeatView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest.RevokeSeatRequestFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting.BackgroundSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting.IBackgroundSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.IRoomSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.RoomSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.IViewPageListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.SeatOrderOperationFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.ISelfSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.SelfSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import cn.rongcloud.voiceroomdemo.mvp.presenter.STATUS_NOT_ON_SEAT
import cn.rongcloud.voiceroomdemo.mvp.presenter.STATUS_ON_SEAT
import cn.rongcloud.voiceroomdemo.mvp.presenter.STATUS_WAIT_FOR_SEAT
import com.rongcloud.common.base.BaseFragment
import com.rongcloud.common.extension.loadImageView
import com.rongcloud.common.extension.loadPortrait
import com.rongcloud.common.extension.ui
import com.rongcloud.common.ui.dialog.ConfirmDialog
import com.rongcloud.common.ui.dialog.TipDialog
import com.rongcloud.common.utils.AccountStore
import com.vanniktech.emoji.EmojiPopup
import dagger.hilt.android.AndroidEntryPoint
import io.rong.callkit.RongCallKit
import io.rong.imkit.utils.RouteUtils
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.MessageContent
import kotlinx.android.synthetic.main.activity_voice_room.*
import kotlinx.android.synthetic.main.activity_voice_room.view.*
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












