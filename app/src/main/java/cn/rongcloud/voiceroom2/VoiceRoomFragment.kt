/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import cn.rong.combusis.feedback.FeedbackHelper
import cn.rong.combusis.manager.AudioPlayManager
import cn.rong.combusis.manager.AudioRecordManager
import cn.rong.combusis.ui.room.widget.like.FavAnimation
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
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.RoomSettingDialogFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.RoomSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.IViewPageListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.SeatOrderOperationFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.ISelfSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.SelfSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import cn.rongcloud.voiceroomdemo.mvp.presenter.STATUS_NOT_ON_SEAT
import cn.rongcloud.voiceroomdemo.mvp.presenter.STATUS_ON_SEAT
import cn.rongcloud.voiceroomdemo.mvp.presenter.STATUS_WAIT_FOR_SEAT
import cn.rongcloud.widget.VoiceRoomMiniManager
import com.rongcloud.common.base.BaseFragment
import com.rongcloud.common.extension.loadImageView
import com.rongcloud.common.extension.loadPortrait
import com.rongcloud.common.extension.ui
import com.rongcloud.common.ui.dialog.ConfirmDialog
import com.rongcloud.common.ui.dialog.TipDialog
import com.rongcloud.common.utils.*
import com.vanniktech.emoji.EmojiPopup
import dagger.hilt.android.AndroidEntryPoint
import io.rong.callkit.RongCallKit

import io.rong.imkit.utils.PermissionCheckUtil
import io.rong.imkit.utils.RongUtils
import io.rong.imkit.utils.RouteUtils
import io.rong.imlib.IMLibExtensionModuleManager
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.HardwareResource
import io.rong.imlib.model.MessageContent
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_voice_room.*
import kotlinx.android.synthetic.main.activity_voice_room.btn_emoji_keyboard
import kotlinx.android.synthetic.main.activity_voice_room.btn_open_send_message
import kotlinx.android.synthetic.main.activity_voice_room.btn_seat_order
import kotlinx.android.synthetic.main.activity_voice_room.cl_input_bar
import kotlinx.android.synthetic.main.activity_voice_room.cl_member_list
import kotlinx.android.synthetic.main.activity_voice_room.container
import kotlinx.android.synthetic.main.activity_voice_room.et_message
import kotlinx.android.synthetic.main.activity_voice_room.iv_background
import kotlinx.android.synthetic.main.activity_voice_room.iv_is_mute
import kotlinx.android.synthetic.main.activity_voice_room.iv_request_enter_seat
import kotlinx.android.synthetic.main.activity_voice_room.iv_room_creator_portrait
import kotlinx.android.synthetic.main.activity_voice_room.iv_room_setting
import kotlinx.android.synthetic.main.activity_voice_room.iv_send_gift
import kotlinx.android.synthetic.main.activity_voice_room.iv_send_message
import kotlinx.android.synthetic.main.activity_voice_room.rv_message_list
import kotlinx.android.synthetic.main.activity_voice_room.rv_seat_list
import kotlinx.android.synthetic.main.activity_voice_room.tv_gift_count
import kotlinx.android.synthetic.main.activity_voice_room.tv_room_creator_name
import kotlinx.android.synthetic.main.activity_voice_room.tv_room_id
import kotlinx.android.synthetic.main.activity_voice_room.tv_room_name
import kotlinx.android.synthetic.main.activity_voice_room.tv_seat_order_operation_number
import kotlinx.android.synthetic.main.activity_voice_room.tv_unread_message_number
import kotlinx.android.synthetic.main.activity_voice_room.view.*
import kotlinx.android.synthetic.main.activity_voice_room.wv_creator_background
import kotlinx.android.synthetic.main.fragment_voice_room.*
import java.util.*
import javax.inject.Inject


private const val TAG = "VoiceRoomFragment"
private const val KEY_ROOM_ID = "KEY_ROOM_INFO_BEAN"
private const val KEY_CREATOR_ID = "KEY_CREATOR_ID"
private const val KEY_IS_CREATE = "KEY_IS_CREATE"

@HiltBinding(value = IVoiceRoomFragmentView::class)
@AndroidEntryPoint
class VoiceRoomFragment : BaseFragment(R.layout.fragment_voice_room), IVoiceRoomFragmentView,
    IMemberListView, IRoomSettingView, IBackgroundSettingView, IViewPageListView, ICreatorView,
    IMemberSettingView, IEmptySeatView, ISelfSettingView, IRevokeSeatView, ISendPresentView,
    IMusicSettingView {

    companion object {
        fun newInstance(
            roomId: String,
            createUserId: String,
            isCreate: Boolean = false
        ): VoiceRoomFragment {
            return VoiceRoomFragment().apply {
                this.arguments = Bundle().apply {
                    putString(KEY_ROOM_ID, roomId)
                    putString(KEY_CREATOR_ID, createUserId)
                    putBoolean(KEY_IS_CREATE, isCreate)
                }
            }
        }
    }

    private lateinit var currentRole: Role

    val roomId by lazy {
        return@lazy arguments?.getString(KEY_ROOM_ID)!!
    }
    val creatorId by lazy {
        return@lazy arguments?.getString(KEY_CREATOR_ID)!!
    }
    val isCreate by lazy {
        return@lazy arguments?.getBoolean(KEY_IS_CREATE, false)!!
    }

    private var memberSettingFragment: MemberSettingFragment? = null

    private var emptySeatFragment: EmptySeatFragment? = null

    private var memberListFragment: MemberListFragment? = null

    private val emojiPopup by lazy {
        EmojiPopup
            .Builder
            .fromRootView(mRootView)
            .setOnEmojiPopupDismissListener {
                btn_emoji_keyboard.setImageResource(R.drawable.ic_voice_room_emoji)
            }
            .setOnEmojiPopupShownListener {
                btn_emoji_keyboard.setImageResource(R.drawable.ic_voice_room_keybroad)
            }
            .build(et_message)
    }

//    private var roomSettingFragment: RoomSettingFragment? = null
    private var roomSettingDialogFragment: RoomSettingDialogFragment? = null

    @Inject
    lateinit var presenter: VoiceRoomFragmentPresenter


    fun getVoiceRoomModel(): VoiceRoomModel {
        return presenter.roomModel
    }

    val favAnimation: FavAnimation by lazy {
        return@lazy FavAnimation(mActivity).apply {
            this.addLikeImages(
                R.drawable.ic_present_0,
                R.drawable.ic_present_1,
                R.drawable.ic_present_2,
                R.drawable.ic_present_3,
                R.drawable.ic_present_4,
                R.drawable.ic_present_5,
                R.drawable.ic_present_6,
                R.drawable.ic_present_7,
                R.drawable.ic_present_8,
                R.drawable.ic_present_9,
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "LifeCycle:onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "LifeCycle:onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "LifeCycle:onPause")
    }

    private val simpleGestureListener: GestureDetector.SimpleOnGestureListener by lazy {
        return@lazy object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                var touch = Point().apply {
                    x = e?.rawX?.toInt() ?: 0
                    y = e?.rawY?.toInt() ?: 0
                }
                showFov(touch)
                presenter.sendFovMessage()
                return true
            }
        }
    }

    /**
     * 显示爱心动画
     */
    override fun showFov(from: Point?) {
        if (from != null) {
            favAnimation.addFavor(container, 300, 1500, from, null)
        } else {
            var location = CustomerPopupWindow.getLocation(iv_send_gift)
            var from =
                Point(location[0] + iv_send_gift.width / 2, location[1] - iv_send_gift.height / 2)
            var to = Point(from.x + 200, from.y - 200)
            favAnimation.addFavor(container, 300, 1200, from, to)
        }
    }

    private var detector: GestureDetector? = null

    override fun initView() {
        // 忽略来电
        RongCallKit.ignoreIncomingCall(true)
        detector = GestureDetector(mActivity, simpleGestureListener).apply {
            this.setIsLongpressEnabled(false)
            this.setOnDoubleTapListener(simpleGestureListener)
        }
        // 初始化角色无关的数据
        btn_open_send_message.setOnClickListener {
            cl_input_bar.isVisible = true
        }
        cl_input_bar.setVisibleChangeListener { _, visibility ->
            if (visibility == View.GONE) {
                hideSoftKeyBoard()
            } else if (visibility == View.VISIBLE) {
                showSoftKeyBoard()
            }
        }
        currentRole = if (creatorId == AccountStore.getUserId()) {
            RoomOwner(mRootView)
        } else {
            Audience(mRootView)
        }

        rv_message_list.adapter = VoiceRoomMessageAdapter(presenter.roomModel) { userId ->
            if (userId == AccountStore.getUserId()) {
                return@VoiceRoomMessageAdapter
            }
            presenter.getMemberInfoByUserId(userId) { member ->
                if (null == member) {
                    showMessage("用户已离开房间")
                    return@getMemberInfoByUserId
                }
                showMemberSetting(member)
            }
        }
        //开启前台服务
        mActivity.startService(
            Intent(
                mActivity,
                cn.rongcloud.voiceroomdemo.mvp.activity.RTCNotificationService::class.java
            )
        )
//        presenter.onCreate()
    }

    override fun onMemberInfoChange() {
        ui {
            // 刷新角色变化
            rv_message_list?.adapter?.notifyDataSetChanged()
        }
    }

    override fun showSingalInfo(int: Int) {
        Log.e(TAG, "showSingalInfo: " + int)
        tv_room_signal.text = int.toString() + "ms";
        if (int in 1..99) {
            tv_room_signal.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_signal_strong,
                0,
                0,
                0
            )
        } else if (int in 100..299) {
            tv_room_signal.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_signal_medium,
                0,
                0,
                0
            )
        } else if (int > 300) {
            tv_room_signal.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_signal_poor,
                0,
                0,
                0
            )
        }
    }

    private fun showSoftKeyBoard() {
        et_message.requestFocus()
        val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(et_message, InputMethodManager.SHOW_IMPLICIT)

    }

    private fun hideSoftKeyBoard() {
        et_message.clearFocus()
        val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(et_message.windowToken, 0)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun initRoleView(roomInfo: UiRoomModel) {
        // 初始化角色相关的视图
        currentRole.initView(roomInfo)
        refreshRoomInfo(roomInfo)

        cl_member_list.setOnClickListener {
            roomInfo.roomBean?.let {
                memberListFragment = MemberListFragment(this, this, it).apply {
                    show(this@VoiceRoomFragment.childFragmentManager)
                }
            }
        }
        iv_room_setting.setOnClickListener {
            roomInfo.roomBean?.let {
                roomSettingDialogFragment = RoomSettingDialogFragment(getVoiceRoomModel(),this);
                roomSettingDialogFragment!!.show(this.childFragmentManager)
//                roomSettingFragment = RoomSettingFragment(this)
//                roomSettingFragment?.show(this@VoiceRoomFragment.childFragmentManager)
            }
        }
        btn_seat_order.setOnClickListener {
            roomInfo.roomBean?.let {
                SeatOrderOperationFragment(this).show(this@VoiceRoomFragment.childFragmentManager)
            }
        }

        iv_send_message.setOnClickListener {
            RouteUtils.routeToSubConversationListActivity(
                mActivity,
                Conversation.ConversationType.PRIVATE,
                "消息"
            )
        }
        iv_send_gift.setOnClickListener {
            roomInfo.roomBean?.let {
                SendPresentFragment(this).show(this@VoiceRoomFragment.childFragmentManager)
            }
        }
//        if (recordVoicePopupWindow==null){
//            recordVoicePopupWindow = RecordVoicePopupWindow(activity) {
//                //发送音频文件
//                Log.e(TAG, "initRoleView: ")
//            }
//        }
//
//        //绑定出发的view
//        recordVoicePopupWindow?.bindView(iv_send_voice_message_id)
        //直接使用imkit里面的代码
        iv_send_voice_message_id.setOnTouchListener(mOnVoiceBtnTouchListener)
        Log.e(TAG, "initRoleView: ")
    }

    private var mConversationType: Conversation.ConversationType =
        Conversation.ConversationType.PRIVATE;

    private val mOnVoiceBtnTouchListener = OnTouchListener { v, event ->
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (!PermissionCheckUtil.checkPermissions(
                v.context,
                permissions
            ) && event.action == MotionEvent.ACTION_DOWN
        ) {
            PermissionCheckUtil.requestPermissions(
                activity,
                permissions,
                PermissionCheckUtil.REQUEST_CODE_ASK_PERMISSIONS
            )
            return@OnTouchListener true
        }
        val location = IntArray(2)
        iv_send_voice_message_id.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        if (event.action == MotionEvent.ACTION_DOWN) {
            Log.e(TAG, ":ACTION_DOWN ")
            //在这里拦截外部的滑动事件
            v.getParent().requestDisallowInterceptTouchEvent(true)
            if (AudioPlayManager.getInstance().isPlaying) {
                AudioPlayManager.getInstance().stopPlay()
            }
            //判断正在视频通话和语音通话中不能进行语音消息发送
            if (RongUtils.phoneIsInUse(v.context) || IMLibExtensionModuleManager.getInstance()
                    .onRequestHardwareResource(HardwareResource.ResourceType.VIDEO)
                || IMLibExtensionModuleManager.getInstance()
                    .onRequestHardwareResource(HardwareResource.ResourceType.AUDIO)
            ) {
                return@OnTouchListener true
            }
            AudioRecordManager.getInstance().startRecord(v.rootView, mConversationType, roomId)
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            if (event.rawX <= 0 || event.rawX > x + v.width || event.rawY < y) {
                AudioRecordManager.getInstance().willCancelRecord()
            } else {
                AudioRecordManager.getInstance().continueRecord()
            }
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            Log.e(TAG, ":ACTION_UP ")
            v.getParent().requestDisallowInterceptTouchEvent(false)
            AudioRecordManager.getInstance().stopRecord()
        }
//        if (mConversationType == Conversation.ConversationType.PRIVATE) {
//            RongIMClient.getInstance().sendTypingStatus(mConversationType, roomId, "RC:VcMsg")
//        }
        true
    }

    private fun sendTextMessage(message: String?) {
        message?.let {
            presenter.sendMessage(it)
        }
    }

    override fun leaveRoomSuccess() {
        ui {
            mActivity.finish()
        }
    }

    override fun onJoinNextRoom(start: Boolean) {
        if (start) {
            showWaitingDialog()
        } else {
            hideWaitingDialog()
        }
    }

    override fun enterSeatSuccess() {
        ui {
            showMessage("上麦成功")
        }
    }

    override fun packupRoom() {
        //先做悬浮窗
        activity?.moveTaskToBack(true)
        //缩放动画,并且显示悬浮窗，在这里要做悬浮窗判断
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //设置一下当前的封面图
        VoiceRoomMiniManager.getInstance().init(activity, activity?.intent)
        VoiceRoomMiniManager.getInstance().refreshRoomOwner(roomId)
        VoiceRoomMiniManager.getInstance()
            .setBackgroudPic(currentRole?.roomInfo.roomBean?.themePictureUrl, activity)
        VoiceRoomMiniManager.getInstance().showMiniWindows()
    }

    override fun refreshOnlineUsersNumber(onlineUsersNumber: Int) {
        currentRole.setOnlineUsersNumber(onlineUsersNumber)
    }

    @SuppressLint("SetTextI18n")
    override fun refreshRoomInfo(roomInfo: UiRoomModel) {
        currentRole.refreshRoomInfo(roomInfo)
        roomInfo.roomBean?.backgroundUrl?.let {
            iv_background.loadImageView(it, R.drawable.default_room_background)
        }
        tv_room_name.text = roomInfo.roomBean?.roomName
        tv_room_id.text = "ID ${roomInfo.roomBean?.id}"
    }

    override fun onSeatInfoChange(index: Int, uiSeatModel: UiSeatModel) {
        Log.d(
            TAG,
            "onSeatInfoChange: $index  mute = ${uiSeatModel.isMute} userId = ${uiSeatModel.userId}"
        )
        if (index == 0) {
            refreshRoomOwner(uiSeatModel)
        } else {
            refreshSeatIndex(index - 1, uiSeatModel)
        }
    }

    private fun refreshRoomOwner(uiSeatModel: UiSeatModel) {
        // TODO: 2021/6/18 根据数据刷新房间所有者的状态
        ui {
            if (uiSeatModel.userId.isNullOrEmpty()) {
                wv_creator_background.stopImmediately()
                iv_room_creator_portrait.loadPortrait(R.drawable.ic_room_creator_not_in_seat)
                iv_room_creator_portrait.background = null
                iv_is_mute.isVisible = uiSeatModel.isMute
                tv_room_creator_name.text = uiSeatModel.userName
            } else {
                iv_room_creator_portrait.loadPortrait(uiSeatModel.portrait)
                iv_room_creator_portrait.setBackgroundResource(R.drawable.bg_voice_room_portrait)
                if (uiSeatModel.isSpeaking && !uiSeatModel.isMute) {
                    wv_creator_background.start()
                } else {
                    wv_creator_background.stop()
                }

                iv_is_mute.isVisible = uiSeatModel.isMute
                tv_room_creator_name.text = uiSeatModel.userName
                tv_gift_count.text = "${uiSeatModel.giftCount}"
            }
        }
    }

    private fun refreshSeatIndex(index: Int, uiSeatModel: UiSeatModel) {
        ui {
            (rv_seat_list.adapter as? VoiceRoomSeatsAdapter)?.refreshIndex(index, uiSeatModel)
        }

    }

    override fun onSeatListChange(uiSeatModelList: List<UiSeatModel>) {
        ui {
            currentRole.onSeatListChange(uiSeatModelList)
            val seatList = uiSeatModelList.subList(1, uiSeatModelList.size)
            rv_seat_list.animation = null
            if (rv_seat_list.adapter == null) {
                rv_seat_list.adapter = VoiceRoomSeatsAdapter { seatModel, position ->
                    // TODO: 2021/6/21 麦位点击事件
                    currentRole.onSeatClick(seatModel, position + 1)
                }.apply {
                    refreshData(seatList)
                    setHasStableIds(true)
                }
            } else {
                (rv_seat_list.adapter as VoiceRoomSeatsAdapter).refreshData(seatList)
            }
        }
    }

    override fun refreshView(uiSeatModel: UiSeatModel) {

    }


    override fun showInviteUserView() {
        presenter.getCurrentRoomInfo().roomBean?.let {
            SeatOrderOperationFragment(this, 1).show(this@VoiceRoomFragment.childFragmentManager)
        }
    }

    override fun sendTextMessageSuccess(message: String) {
        if (et_message.text.toString() == message) {
            et_message.setText("")
        }
    }

    override fun showChatRoomMessage(messageContent: MessageContent) {
        ui {
            (rv_message_list.adapter as? VoiceRoomMessageAdapter)?.addMessage(messageContent)

            rv_message_list.post {
                rv_message_list.adapter?.let {
                    rv_message_list.smoothScrollToPosition(it.itemCount - 1)
                }
            }
        }
    }

    var confirmDialog: ConfirmDialog? = null

    override fun showPickReceived(isCreateReceive: Boolean, userId: String) {
        ui {
            var current: ConfirmDialog? = null
            ConfirmDialog(
                mActivity,
                "您被${if (isCreateReceive) "房主" else "管理员"}邀请上麦，是否同意?",
                true,
                "同意",
                "拒绝",
                cancelBlock = {
                    presenter.refuseInvite(userId)
                }) {
                presenter.enterSeatIfAvailable()
            }.apply {
                current = this
                show()
            }
            // 处理重复显示 在消失
            mRootView.postDelayed({
                confirmDialog?.dismiss()
                confirmDialog = current
            }, 500)
        }
    }

    override fun showRoomClose() {
        ui {
            TipDialog(
                mActivity,
                "当前直播已结束",
                listener = {
                    presenter.leaveRoom()
                }).apply {
                show()
            }
        }
    }

    override fun switchToAdminRole(isAdmin: Boolean, roomInfo: UiRoomModel) {
        if (currentRole is Audience && currentRole !is Admin && isAdmin) {
            currentRole = Admin(mRootView).apply {
                initView(roomInfo)
            }
        } else if (currentRole is Admin && !isAdmin) {
            currentRole = Audience(mRootView).apply {
                initView(roomInfo)
            }
        }
    }

    override fun changeStatus(status: Int) {
        when (status) {
            STATUS_NOT_ON_SEAT -> {
                iv_request_enter_seat.setImageResource(R.drawable.ic_request_enter_seat)
            }
            STATUS_WAIT_FOR_SEAT -> {
                iv_request_enter_seat.setImageResource(R.drawable.ic_wait_enter_seat)
            }
            STATUS_ON_SEAT -> {
                iv_request_enter_seat.setImageResource(R.drawable.ic_on_seat)
            }
        }
    }

    override fun showUnReadRequestNumber(number: Int) {
        currentRole.showUnReadRequestNumber(number)
    }

    /**
     * 显示未读消息的数量
     */
    override fun showUnreadMessage(count: Int) {
        tv_unread_message_number.isVisible = count > 0
        tv_unread_message_number.text = if (count < 99) {
            "$count"
        } else {
            "99+"
        }
    }


    override fun initData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        // 统计打分
        FeedbackHelper.getHelper().statistics()
        // 取消 忽略来电
        RongCallKit.ignoreIncomingCall(false)
        favAnimation.let {
            it.release()
        }
        mActivity.stopService(
            Intent(
                mActivity,
                cn.rongcloud.voiceroomdemo.mvp.activity.RTCNotificationService::class.java
            )
        )
//        presenter.onDestroy()

    }

    override fun onJoinRoomSuccess() {
        currentRole.onJoinRoomSuccess()
        hideWaitingDialog()
    }

    fun onBackPressed() {
        currentRole.onTopRightButtonPress()
    }

    fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (null != cl_input_bar && cl_input_bar.isVisible) {
            val rect = Rect()
            cl_input_bar.getGlobalVisibleRect(rect)
            if (!rect.contains(ev?.rawX?.toInt() ?: 0, ev?.rawY?.toInt() ?: 0)) {
                cl_input_bar.isVisible = false
                return true
            }
        }
        detector?.let {
            if (it.onTouchEvent(ev)) {
                return true
            }
        }
        return false
    }

    override fun showBackgroundFragment() {
        roomSettingDialogFragment?.dismiss()
        val roomInfoBean = presenter.getCurrentRoomInfo().roomBean
        roomInfoBean?.let {
            BackgroundSettingFragment(this).show(this@VoiceRoomFragment.childFragmentManager)
        }
    }

    override fun showMusicSettingFragment() {
        roomSettingDialogFragment?.dismiss()
        val roomInfoBean = presenter.getCurrentRoomInfo().roomBean
        roomInfoBean?.let {
            MusicSettingFragment(this).show(this@VoiceRoomFragment.childFragmentManager)
        }
    }

    override fun hideSettingView() {
        super.hideSettingView()
        roomSettingDialogFragment?.dismiss()
    }

    override fun fragmentDismiss() {
        memberSettingFragment?.dismiss()
    }

    override fun sendGift(userId: String) {
        memberSettingFragment?.dismiss()
        memberListFragment?.dismiss()
        SendPresentFragment(
            this,
            arrayListOf<String>(userId)
        ).show(this@VoiceRoomFragment.childFragmentManager)
    }


    override fun showRevokeSeatRequest() {
        when (presenter.currentStatus) {
            STATUS_ON_SEAT -> {

            }
            STATUS_WAIT_FOR_SEAT -> {
                RevokeSeatRequestFragment(this).show(
                    this@VoiceRoomFragment.childFragmentManager
                )
            }
            STATUS_NOT_ON_SEAT -> {
                presenter.enterSeat(-1)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下为不同角色的状态类
    ///////////////////////////////////////////////////////////////////////////
    var lastOnlineCount: Int = 0//记录上次 处理切换角色的默认0的问题

    abstract inner class Role(val view: View) {
        open fun initView(roomInfo: UiRoomModel) {
            this.roomInfo = roomInfo
            setOnlineUsersNumber(lastOnlineCount)
        }

        lateinit var roomInfo: UiRoomModel

        open fun initListener() {
            with(view) {
                iv_right_button.setOnClickListener {
                    onTopRightButtonPress()
                }
                iv_request_enter_seat.setOnClickListener {
                    showRevokeSeatRequest()
                }

                btn_emoji_keyboard.setOnClickListener {
                    emojiPopup.toggle()
                }

                btn_send_message.setOnClickListener {
                    sendMessage()
                }

                et_message.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendMessage()
                    }
                    return@setOnEditorActionListener false
                }
            }
        }

        fun sendMessage() {
            var msg = et_message.text.toString().trim()
            if (msg.isNullOrEmpty()) {
                showMessage("消息不能为空")
                return
            }
            sendTextMessage(msg)
        }

        @SuppressLint("SetTextI18n")
        open fun setOnlineUsersNumber(number: Int) {
            with(view) {
                tv_room_members_count.text = "在线 $number"
                lastOnlineCount = number
            }
        }

        open fun onTopRightButtonPress() {}

        open fun onJoinRoomSuccess() {

        }

        open fun refreshRoomInfo(roomInfo: UiRoomModel) {
            // TODO: 2021/6/21 房间信息发生改变
        }

        open fun onSeatListChange(uiSeatModelList: List<UiSeatModel>) {

        }

        abstract fun onSeatClick(seatModel: UiSeatModel, position: Int)

        open fun showUnReadRequestNumber(number: Int) {
            tv_seat_order_operation_number.isVisible = false

        }
    }


    /**
     * 观众
     */
    open inner class Audience(
        view: View
    ) :
        Role(view) {

        override fun initView(roomInfo: UiRoomModel) {
            super.initView(roomInfo)
            with(view) {
                iv_right_button.setImageResource(R.drawable.ic_close_right_top_icon)
                iv_request_enter_seat.isVisible = true
                iv_room_setting.isVisible = false
                btn_seat_order.isVisible = false
                tv_seat_order_operation_number.isVisible = false
            }

            initListener()
        }

        override fun onSeatClick(seatModel: UiSeatModel, position: Int) {
            if (seatModel.seatStatus == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking) {
                // 点击锁定座位
                showMessage("该座位已锁定")
            } else if (seatModel.seatStatus == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty) {
                // 点击空座位
                presenter.enterSeat(position)
            } else if (seatModel.seatStatus == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing) {
                if (seatModel.userId == AccountStore.getUserId()) {
                    // 点击自己头像
                    SelfSettingFragment(this@VoiceRoomFragment, seatModel, roomId).show(
                        this@VoiceRoomFragment.childFragmentManager
                    )
                } else {
                    // 点击别人头像
                    presenter.getCurrentRoomInfo().roomBean?.let { roomInfo ->
                        seatModel.member?.let { memberInfo ->
                            MemberSettingFragment(
                                this@VoiceRoomFragment,
                                roomInfo,
                                memberInfo
                            ).show(this@VoiceRoomFragment.childFragmentManager)
                        }
                    }
                }
            }
        }

        override fun onTopRightButtonPress() {
            presenter.leaveRoom()
        }

    }

    fun showMemberSetting(member: UiMemberModel) {
        presenter.getCurrentRoomInfo().roomBean?.let { roomBean ->
            memberSettingFragment = MemberSettingFragment(
                this,
                roomBean,
                member
            ).apply {
                show(this@VoiceRoomFragment.childFragmentManager)
            }
        }
    }

    /**
     * 房间创建者
     */
    inner class RoomOwner(
        view: View
    ) : Role(view) {
        private var exitRoomPopupWindow: ExitRoomPopupWindow? = null
        private var isFirstEnterSeat = true
        override fun initView(roomInfo: UiRoomModel) {
            super.initView(roomInfo)
            with(view) {
                iv_right_button.setImageResource(R.drawable.ic_more_right_top_icon)
                iv_request_enter_seat.isVisible = false
                iv_room_setting.isVisible = true
                btn_seat_order.isVisible = true
                tv_seat_order_operation_number.isVisible = true
                iv_send_voice_message_id.isVisible = false
            }
            initListener()
        }

        override fun initListener() {
            super.initListener()
            with(view) {
                iv_room_creator_portrait.setOnClickListener {
                    presenter.getCurrentSeatsInfo().elementAtOrNull(0)?.let { model ->
                        if (model.userId == AccountStore.getUserId()) {
                            roomInfo.roomBean?.let {
                                CreatorSettingFragment(
                                    this@VoiceRoomFragment,
                                    it
                                ).show(this@VoiceRoomFragment.childFragmentManager)
                            }
                        } else {
                            presenter.roomOwnerEnterSeat()
                        }
                        RCVoiceRoomEngine.getInstance().updateSeatInfo(1, "update", null)
                    }
                }
            }
        }

        override fun onSeatListChange(uiSeatModelList: List<UiSeatModel>) {
            super.onSeatListChange(uiSeatModelList)
            if (isFirstEnterSeat) {
                isFirstEnterSeat = false
                presenter.roomOwnerEnterSeat()
            }

        }

        override fun onSeatClick(seatModel: UiSeatModel, position: Int) {
            when (seatModel.seatStatus) {
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty, RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking -> {
                    roomInfo.roomBean?.let { roomBean ->
                        emptySeatFragment = EmptySeatFragment(
                            this@VoiceRoomFragment,
                            seatModel,
                            roomBean.roomId
                        ).apply {
                            show(this@VoiceRoomFragment.childFragmentManager)
                        }
                    }
                }
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing -> {
                    roomInfo.roomBean?.let { roomBean ->
                        seatModel.member?.let { member ->
                            memberSettingFragment = MemberSettingFragment(
                                this@VoiceRoomFragment,
                                roomBean,
                                member
                            ).apply {
                                show(this@VoiceRoomFragment.childFragmentManager)
                            }
                        }
                    }
                }
            }
        }

        override fun onTopRightButtonPress() {
            view.post {
                with(view) {
                    exitRoomPopupWindow = ExitRoomPopupWindow(view.context, leaveRoomBlock = {
                        exitRoomPopupWindow?.dismiss()
                        presenter.leaveRoom()

                    }, closeRoomBlock = {
                        exitRoomPopupWindow?.dismiss()
                        ConfirmDialog(context, "确定结束本次直播吗？", true) {
                            presenter.closeRoom()
                        }.show()
                    }, packUpRoomBlock = {
                        exitRoomPopupWindow?.dismiss()
                        presenter.packUpRoom()

                    })
                    exitRoomPopupWindow?.setAnimationStyle(R.style.popup_window_anim_style);
                    exitRoomPopupWindow?.showAtLocation(
                        iv_background,
                        Gravity.TOP,
                        0,
                        0
                    )
                }
            }
        }

        override fun showUnReadRequestNumber(number: Int) {
            with(view) {
                tv_seat_order_operation_number.isVisible = number > 0
                tv_seat_order_operation_number.text = if (number < 10) "$number" else "$9+"
            }
        }

    }

    /**
     * 管理员
     */
    inner class Admin(
        view: View
    ) :
        Audience(view) {
        override fun onSeatClick(seatModel: UiSeatModel, position: Int) {
            when (seatModel.seatStatus) {
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking -> {
                    showMessage("该座位已锁定")
                }
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty -> {
                    presenter.enterSeat(seatModel.index)
                }
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing -> {
                    if (seatModel.userId == AccountStore.getUserId()) {
                        SelfSettingFragment(this@VoiceRoomFragment, seatModel, roomId).show(
                            this@VoiceRoomFragment.childFragmentManager
                        )
                    } else {
                        roomInfo.roomBean?.let { roomBean ->
                            seatModel.member?.let { member ->
                                memberSettingFragment = MemberSettingFragment(
                                    this@VoiceRoomFragment,
                                    roomBean,
                                    member
                                ).apply {
                                    show(this@VoiceRoomFragment.childFragmentManager)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}












