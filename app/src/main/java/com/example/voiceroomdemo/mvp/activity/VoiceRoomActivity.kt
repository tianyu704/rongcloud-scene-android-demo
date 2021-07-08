/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.*
import com.example.voiceroomdemo.mvp.activity.iview.IVoiceRoomView
import com.example.voiceroomdemo.mvp.adapter.VoiceRoomMessageAdapter
import com.example.voiceroomdemo.mvp.adapter.VoiceRoomSeatsAdapter
import com.example.voiceroomdemo.mvp.fragment.present.ISendPresentView
import com.example.voiceroomdemo.mvp.fragment.present.SendPresentFragment
import com.example.voiceroomdemo.mvp.fragment.present.like.FavAnimation
import com.example.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting.CreatorSettingFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting.ICreatorView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting.EmptySeatFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting.IEmptySeatView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.memberlist.IMemberListView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.memberlist.MemberListFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting.IMemberSettingView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting.MemberSettingFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.IMusicSettingView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.MusicSettingFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest.IRevokeSeatView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest.RevokeSeatRequestFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting.BackgroundSettingFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting.IBackgroundSettingView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.IRoomSettingView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.RoomSettingFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.IViewPageListView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.SeatOrderOperationFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.ISelfSettingView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.SelfSettingFragment
import com.example.voiceroomdemo.mvp.presenter.STATUS_NOT_ON_SEAT
import com.example.voiceroomdemo.mvp.presenter.STATUS_ON_SEAT
import com.example.voiceroomdemo.mvp.presenter.STATUS_WAIT_FOR_SEAT
import com.example.voiceroomdemo.mvp.presenter.VoiceRoomPresenter
import com.example.voiceroomdemo.ui.dialog.ConfirmDialog
import com.example.voiceroomdemo.ui.popupwindow.ExitRoomPopupWindow
import com.example.voiceroomdemo.ui.uimodel.UiRoomModel
import com.example.voiceroomdemo.ui.uimodel.UiSeatModel
import com.vanniktech.emoji.EmojiPopup
import io.rong.imkit.utils.RouteUtils
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.MessageContent
import kotlinx.android.synthetic.main.activity_voice_room.*
import kotlinx.android.synthetic.main.activity_voice_room.view.*


private const val TAG = "VoiceRoomActivity"

private const val KEY_ROOM_ID = "KEY_ROOM_INFO_BEAN"
private const val KEY_CREATOR_ID = "KEY_CREATOR_ID"

class VoiceRoomActivity : BaseActivity<VoiceRoomPresenter, IVoiceRoomView>(), IVoiceRoomView,
    IMemberListView, IRoomSettingView, IBackgroundSettingView, IViewPageListView, ICreatorView,
    IMemberSettingView, IEmptySeatView, ISelfSettingView, IRevokeSeatView, ISendPresentView,
    IMusicSettingView {


    companion object {
        fun startActivity(context: Context, roomId: String, createUserId: String) {
            Intent(context, VoiceRoomActivity::class.java).apply {
                putExtra(KEY_ROOM_ID, roomId)
                putExtra(KEY_CREATOR_ID, createUserId)
                context.startActivity(this)
            }
        }
    }

    private lateinit var currentRole: Role

    private lateinit var roomId: String
    private lateinit var creatorId: String

    private var memberSettingFragment: MemberSettingFragment? = null

    private var emptySeatFragment: EmptySeatFragment? = null

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

    private var roomSettingFragment: RoomSettingFragment? = null

    override fun initPresenter(): VoiceRoomPresenter {
        return VoiceRoomPresenter(this, roomId)
    }

    override fun isLightThemeActivity(): Boolean {
        return false
    }

    val favAnimation: FavAnimation by lazy {
        return@lazy FavAnimation(this).apply {
            this.addLikeImages(
                R.drawable.heart0,
                R.drawable.heart1,
                R.drawable.heart2,
                R.drawable.heart3,
                R.drawable.heart4
            )
        }
    }

    private val simpleGestureListener: GestureDetector.SimpleOnGestureListener by lazy {
        return@lazy object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                var touch = Point().apply {
                    x = e?.rawX?.toInt() ?: 0
                    y = e?.rawY?.toInt() ?: 0
                }
                showFov(touch)
                return true
            }
        }
    }

    /**
     * 显示爱心动画
     */
    fun showFov(from: Point?) {
        if (from != null) {
            favAnimation.addFavor(container, 300, 1500, from, null)
        } else {
            var sp = Point()
            windowManager.defaultDisplay.getRealSize(sp)
            var from = Point(sp.x - 200, sp.y - 100)
            var to = Point(sp.x - 100, sp.y - 200)
            favAnimation.addFavor(container, 300, 1500, from, to)
        }
    }

    private var detector: GestureDetector? = null

    override fun getContentView(): Int = R.layout.activity_voice_room

    override fun initView() {
        detector = GestureDetector(this, simpleGestureListener).apply {
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

        rv_message_list.adapter = VoiceRoomMessageAdapter()
    }

    private fun showSoftKeyBoard() {
        et_message.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(et_message, InputMethodManager.SHOW_IMPLICIT)

    }

    private fun hideSoftKeyBoard() {
        et_message.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(et_message.windowToken, 0)
    }

    override fun beforeInitView() {
        roomId = intent.getStringExtra(KEY_ROOM_ID)!!
        creatorId = intent.getStringExtra(KEY_CREATOR_ID)!!
    }

    override fun initRoleView(roomInfo: UiRoomModel) {
        // 初始化角色相关的视图
        currentRole.initView(roomInfo)
        refreshRoomInfo(roomInfo)

        cl_member_list.setOnClickListener {
            roomInfo.roomBean?.let {
                MemberListFragment(this, it).show(supportFragmentManager)
            }
        }
        iv_room_setting.setOnClickListener {
            roomInfo.roomBean?.let {
                roomSettingFragment = RoomSettingFragment(this, it)
                roomSettingFragment?.show(supportFragmentManager)
            }
        }
        btn_seat_order.setOnClickListener {
            roomInfo.roomBean?.let {
                SeatOrderOperationFragment(this, it).show(supportFragmentManager)
            }
        }

        iv_send_message.setOnClickListener {
            RouteUtils.routeToSubConversationListActivity(
                this,
                Conversation.ConversationType.PRIVATE,
                "消息"
            )
        }
        iv_send_gift.setOnClickListener {
            roomInfo.roomBean?.let {
                SendPresentFragment(this, it.roomId).show(supportFragmentManager)
            }
        }
    }

    private fun sendTextMessage(message: String?) {
        message?.let {
            presenter.sendMessage(it)
        }
    }

    override fun leaveRoomSuccess() {
        ui {
            finish()
        }
    }

    override fun enterSeatSuccess() {
        ui {
            showToast("上麦成功")
        }
    }

    override fun refreshOnlineUsersNumber(onlineUsersNumber: Int) {
        currentRole.setOnlineUsersNumber(onlineUsersNumber)
    }

    @SuppressLint("SetTextI18n")
    override fun refreshRoomInfo(roomInfo: UiRoomModel) {
        Log.d(TAG, "refreshRoomInfo: $roomInfo")
        currentRole.refreshRoomInfo(roomInfo)
        roomInfo.roomBean?.backgroundUrl?.let {
            iv_background.loadImageView(it, R.drawable.default_room_background)
        }
        tv_room_name.text = roomInfo.roomBean?.roomName
        tv_room_id.text = "ID ${roomInfo.roomBean?.id}"
    }

    override fun onSeatInfoChange(index: Int, uiSeatModel: UiSeatModel) {
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
                iv_is_mute.isVisible = true
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
            SeatOrderOperationFragment(this, it, 1).show(supportFragmentManager)
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

    override fun showPickReceived(isCreateReceive: Boolean, userId: String) {
        ui {
            ConfirmDialog(
                this,
                "您被${if (isCreateReceive) "房主" else "管理员"}邀请上麦，是否同意?",
                false,
                "同意",
                "拒绝",
                cancelBlock = {
                    presenter.refuseInvite(userId)
                }) {
                presenter.enterSeatIfAvailable()
            }.show()
        }
    }

    override fun switchToAdminRole(isAdmin: Boolean) {
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

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        favAnimation?.let {
            it.release()
        }
    }

    override fun onJoinRoomSuccess() {
        currentRole.onJoinRoomSuccess()
    }

    override fun onBackPressed() {
        currentRole.onTopRightButtonPress()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.e(TAG, "dispatchTouchEvent")
        if (ev?.action == KeyEvent.ACTION_DOWN && cl_input_bar.isVisible) {
            val rect = Rect()
            cl_input_bar.getGlobalVisibleRect(rect)
            if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                cl_input_bar.isVisible = false
                return true
            }
        }
        detector?.let {
            if (it.onTouchEvent(ev)) {
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun showBackgroundFragment() {
        roomSettingFragment?.dismiss()
        val roomInfoBean = presenter.getCurrentRoomInfo().roomBean
        roomInfoBean?.let {
            BackgroundSettingFragment(it, this).show(supportFragmentManager)
        }
    }

    override fun showMusicSettingFragment() {
        roomSettingFragment?.dismiss()
        val roomInfoBean = presenter.getCurrentRoomInfo().roomBean
        roomInfoBean?.let {
            MusicSettingFragment(this).show(supportFragmentManager)
        }
    }

    override fun hideSettingView() {
        super.hideSettingView()
        roomSettingFragment?.dismiss()
    }

    override fun fragmentDismiss() {
        memberSettingFragment?.dismiss()
    }


    ///////////////////////////////////////////////////////////////////////////
    // 以下为不同角色的状态类
    ///////////////////////////////////////////////////////////////////////////

    abstract inner class Role(val view: View) {
        lateinit var roomInfo: UiRoomModel
        open fun initView(roomInfo: UiRoomModel) {
            this.roomInfo = roomInfo
            with(view) {
                setOnlineUsersNumber(0)
            }
        }

        open fun initListener() {
            with(view) {
                iv_right_button.setOnClickListener {
                    onTopRightButtonPress()
                }
                iv_request_enter_seat.setOnClickListener {
                    when (presenter.currentStatus) {
                        STATUS_ON_SEAT -> {

                        }
                        STATUS_WAIT_FOR_SEAT -> {
                            RevokeSeatRequestFragment(this@VoiceRoomActivity, roomId).show(
                                supportFragmentManager
                            )
                        }
                        STATUS_NOT_ON_SEAT -> {
                            presenter.enterSeat(-1)
                        }
                    }
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
            sendTextMessage(et_message.text.toString())
        }

        @SuppressLint("SetTextI18n")
        open fun setOnlineUsersNumber(number: Int) {
            with(view) {
                tv_room_members_count.text = "在线 $number"
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

        override fun onJoinRoomSuccess() {
            super.onJoinRoomSuccess()
        }

        override fun onSeatClick(seatModel: UiSeatModel, position: Int) {
            if (seatModel.seatStatus == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty) {
                // 点击空座位
                presenter.enterSeat(position)
            } else if (seatModel.seatStatus == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed) {
                if (seatModel.userId == AccountStore.getUserId()) {
                    // 点击自己头像
                    SelfSettingFragment(this@VoiceRoomActivity, seatModel, roomId).show(
                        supportFragmentManager
                    )
                } else {
                    // 点击别人头像
                    presenter.getCurrentRoomInfo().roomBean?.let { roomInfo ->
                        seatModel.member?.let { memberInfo ->
                            MemberSettingFragment(
                                this@VoiceRoomActivity,
                                roomInfo,
                                memberInfo
                            ).show(supportFragmentManager)
                        }
                    }
                }
            }
        }

        override fun onTopRightButtonPress() {
            presenter.leaveRoom()
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
            }
            initListener()
        }

        override fun onJoinRoomSuccess() {
            super.onJoinRoomSuccess()
        }

        override fun initListener() {
            super.initListener()
            with(view) {
                iv_room_creator_portrait.setOnClickListener {
                    presenter.getCurrentSeatsInfo().elementAtOrNull(0)?.let { model ->
                        if (model.userId == AccountStore.getUserId()) {
                            roomInfo.roomBean?.let {
                                CreatorSettingFragment(
                                    this@VoiceRoomActivity,
                                    it
                                ).show(this@VoiceRoomActivity.supportFragmentManager)
                            }
                        } else {
                            presenter.roomOwnerEnterSeat()
                        }
                    }
                }
            }
        }

        override fun refreshRoomInfo(roomInfo: UiRoomModel) {
            super.refreshRoomInfo(roomInfo)

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
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty, RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLock -> {
                    roomInfo.roomBean?.let { roomBean ->
                        emptySeatFragment = EmptySeatFragment(
                            this@VoiceRoomActivity,
                            seatModel,
                            roomBean.roomId
                        ).apply {
                            show(supportFragmentManager)
                        }
                    }
                }
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed -> {
                    roomInfo.roomBean?.let { roomBean ->
                        seatModel.member?.let { member ->
                            memberSettingFragment = MemberSettingFragment(
                                this@VoiceRoomActivity,
                                roomBean,
                                member
                            ).apply {
                                show(supportFragmentManager)
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
                    })

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
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty, RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLock -> {
                    roomInfo.roomBean?.let { roomBean ->
                        emptySeatFragment = EmptySeatFragment(
                            this@VoiceRoomActivity,
                            seatModel,
                            roomBean.roomId
                        ).apply {
                            show(supportFragmentManager)
                        }
                    }
                }
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed -> {
                    if (seatModel.userId == AccountStore.getUserId()) {
                        SelfSettingFragment(this@VoiceRoomActivity, seatModel, roomId).show(
                            supportFragmentManager
                        )
                    } else {
                        roomInfo.roomBean?.let { roomBean ->
                            seatModel.member?.let { member ->
                                memberSettingFragment = MemberSettingFragment(
                                    this@VoiceRoomActivity,
                                    roomBean,
                                    member
                                ).apply {
                                    show(supportFragmentManager)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}












