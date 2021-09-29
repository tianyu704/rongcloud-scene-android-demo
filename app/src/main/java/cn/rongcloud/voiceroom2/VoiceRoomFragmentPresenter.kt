/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom2

import android.util.Log
import androidx.fragment.app.Fragment
import cn.rong.combusis.manager.RCChatRoomMessageManager
import cn.rong.combusis.message.*
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo
import cn.rongcloud.voiceroom.net.VoiceRoomNetManager
import cn.rongcloud.voiceroom.ui.uimodel.UiMemberModel
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel
import cn.rongcloud.voiceroom.utils.AudioEffectManager
import cn.rongcloud.voiceroom.utils.JsonUtils
import cn.rongcloud.voiceroomdemo.mvp.model.*
import cn.rongcloud.voiceroomdemo.utils.DefaultConfigConstant
import com.kit.UIKit
import com.rongcloud.common.base.BaseLifeCyclePresenter
import com.rongcloud.common.extension.isNotNullOrEmpty
import com.rongcloud.common.net.ApiConstant
import com.rongcloud.common.utils.AccountStore
import com.rongcloud.common.utils.AudioManagerUtil
import dagger.hilt.android.scopes.FragmentScoped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.rong.imlib.IRongCoreListener
import io.rong.imlib.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

/**
 * @author gusd
 * @Date 2021/06/10
 */
const val STATUS_ON_SEAT = 0
const val STATUS_NOT_ON_SEAT = 1
const val STATUS_WAIT_FOR_SEAT = 2

@FragmentScoped
class VoiceRoomFragmentPresenter @Inject constructor(
    val view: IVoiceRoomFragmentView,
    @Named("roomId") private val roomId: String,
    @Named("isCreate") private val isCreate: Boolean,
    val roomModel: VoiceRoomModel,
    fragment: Fragment
) : BaseLifeCyclePresenter(fragment), IRongCoreListener.OnReceiveMessageListener {

    var currentStatus = STATUS_NOT_ON_SEAT

    private lateinit var currentUserId: String


    private var hasInit = false
    private var currentRoomInfo: UiRoomModel? = null

    override fun onDestroy() {
        Log.d(TAG, "LifeCycle:onDestroy roomId = $roomId")
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(null)
        RCVoiceRoomEngine.getInstance().removeMessageReceiveListener(this)
        AudioManagerUtil.dispose()
    }

    override fun onResume() {
        Log.d(TAG, "LifeCycle:onResume roomId = $roomId")
        roomModel.noticeRefreshUnreadMessageCount()
    }

    override fun onStart() {
        Log.d(TAG, "LifeCycle:onStart roomId = $roomId")
    }

    override fun onStop() {
        Log.d(TAG, "LifeCycle:onStop roomId = $roomId")
    }


    override fun onCreate() {
        Log.d(TAG, "LifeCycle:onCreate roomId = $roomId")
        // 监听房间信息变化
        addDisposable(roomModel
            .obRoomInfoChange()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { roomInfo ->
                    Log.e(TAG, "obRoomInfoChange:" + JsonUtils.toJson(roomInfo))
                    currentRoomInfo = roomInfo
                    if (!hasInit) {
                        hasInit = true
                        view.initRoleView(roomInfo)
                        initRoomEventListener()
                        joinRoom()
                        currentUserId = AccountStore.getUserId() ?: ""
                    } else {
                        view.refreshRoomInfo(roomInfo)
                    }

                }, {
                    view.showError(it.message)
                    view.leaveRoomSuccess()
                }
            ))

    }

    private fun afterInitView() {
        // 监听座位列表变化
        addDisposable(roomModel
            .obSeatListChange()
            .subscribe { list ->
                Log.v(TAG, "obSeatListChange:" + JsonUtils.toJson(list))
                view.onSeatListChange(list)
                list.forEach {
                    if (it.userId.isNotNullOrEmpty() && (it.member?.member == null)) {
                        val memberInfo =
                            roomModel.getMemberInfoByUserIdOnlyLocal(it.userId)
                        if (memberInfo?.member == null) {
                            roomModel.refreshAllMemberInfoList()
                        } else {
                            it.member = memberInfo
                        }
                    }
                }
            })

        // 监听全部座位信息变化
        addDisposable(roomModel
            .obSeatInfoChange()
            .subscribe { info ->
                Log.d(TAG, "onCreate: obSeatInfoChange")
                if ((info.member?.member == null) && !info.userId.isNullOrEmpty()) {
                    val memberInfo =
                        roomModel.getMemberInfoByUserIdOnlyLocal(info.userId)
                    if (memberInfo?.member == null) {
                        roomModel.refreshAllMemberInfoList()
                    } else {
                        info.member = memberInfo
                    }
                }
                view.onSeatInfoChange(info.index, info)
            })

        // 监听在线人数变化
        addDisposable(roomModel
            .obOnlineUserCount()
            .subscribe { count ->
                view.refreshOnlineUsersNumber(count)
            })

        // 监听房间事件
        addDisposable(roomModel
            .obRoomEventChange()
            .subscribe {
                handleRoomEvent(it)
            })

        // 监听房间成员列表变化
        addDisposable(roomModel
            .obMemberListChange()
            // 因为开销较大，防止过快调用
            .debounce(10, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.d(TAG, "obMemberListChange: " + JsonUtils.toJson(it))
                it.forEach { member ->
                    roomModel.getSeatInfoByUserId(member.userId)?.member = member
                }
                view.onMemberInfoChange()
            })

        // 监听房间成员信息变化
        addDisposable(roomModel
            .obMemberInfoChange()
            .subscribe {
                Log.d(TAG, "onCreate: obMemberInfoChange")
                roomModel.getSeatInfoByUserId(it.userId)?.let { seatModel ->
                    seatModel?.member = it
                    if (it.userId == AccountStore.getUserId()) {
                        // 监听当前用户是否为管理员
                        if (roomModel.currentUIRoomInfo.roomBean?.createUser?.userId != AccountStore.getUserId()) {
                            view.switchToAdminRole(it.isAdmin, roomModel.currentUIRoomInfo)
                        }
                    }
                    //同步修改麦位上的管理员角色
                    view.onSeatInfoChange(seatModel.index, seatModel)
                }
            })

        // 监听自己在座位上的变化
        addDisposable(roomModel
            .obSeatInfoChange()
            .map {
                var inSeat = false
                roomModel.currentUISeatInfoList.forEach { model ->
                    if (model.userId == AccountStore.getUserId()) inSeat = true
                }
                return@map inSeat
            }
            .subscribe {
                Log.d(TAG, "onCreate: obSeatInfoChange")
                if (it) {
                    if (currentStatus == STATUS_WAIT_FOR_SEAT) {
                        RCVoiceRoomEngine.getInstance()
                            .cancelRequestSeat(object : RCVoiceRoomCallback {
                                override fun onError(code: Int, message: String?) {

                                }

                                override fun onSuccess() {
                                }

                            })
                    }
                    currentStatus = STATUS_ON_SEAT
                    view.changeStatus(STATUS_ON_SEAT)
                } else {
                    if (currentStatus != STATUS_WAIT_FOR_SEAT) {
                        currentStatus = STATUS_NOT_ON_SEAT
                        view.changeStatus(STATUS_NOT_ON_SEAT)
                    }
                }
            })
        // 监听房间消息
        addDisposable(
            RCChatRoomMessageManager
                .obMessageReceiveByRoomId(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is RCChatroomVoice, is RCChatroomLocationMessage, is RCChatroomBarrage, is RCChatroomEnter, is RCChatroomKickOut, is RCChatroomGiftAll, is RCChatroomGift, is RCChatroomAdmin, is RCChatroomSeats -> {
                            view.showChatRoomMessage(it)
                            if (it is RCChatroomGiftAll || it is RCChatroomGift) {
                                roomModel.refreshGift()
                            }
                            if (it is RCChatroomEnter) {
                                val member =
                                    roomModel.getMemberInfoByUserIdOnlyLocal(it.userId)
                                if (member?.member == null) {
                                    roomModel.refreshAllMemberInfoList()
                                }
                            }
                        }
                        is RCChatroomLike -> {
                            view.showFov(null)
                        }
                    }
                })

        // 监听上麦邀请
        addDisposable(
            roomModel
                .obOnPickSeatReceived()
                .subscribe {
                    view.showPickReceived(
                        it == getCurrentRoomInfo().roomBean?.createUser?.userId,
                        it
                    )
                }
        )

        addDisposable(
            roomModel
                .obUnreadMessageNumberChange()
                .subscribe {
                    view.showUnreadMessage(it)
                }
        )
        //信号监听
        addDisposable(roomModel.obOnNetworkStatusChange()
            .subscribe{
                view.showSingalInfo(it)
            })
        addDisposable(roomModel
            .obRequestSeatListChange()
            .map {
                return@map it.size
            }
            .distinctUntilChanged()
            .subscribe { number ->
                view.showUnReadRequestNumber(number)
            })
        currentStatus = STATUS_NOT_ON_SEAT
        view.changeStatus(STATUS_NOT_ON_SEAT)
    }

    private fun handleRoomEvent(eventInfo: Pair<String, ArrayList<String>>) {
        when (eventInfo.first) {
            EVENT_ROOM_CLOSE -> {
//                view.showMessage("房间已关闭")
//                leaveRoom()
                view.showRoomClose()
            }
            EVENT_BACKGROUND_CHANGE -> {
                roomModel.refreshRoomInfo()
            }
            EVENT_MANAGER_LIST_CHANGE -> {
                roomModel.refreshAdminList()
            }
            EVENT_REJECT_MANAGE_PICK -> {
                Log.d(TAG, "handleRoomEvent: EVENT_REJECT_MANAGE_PICK")
//                // 拒绝之后重置状态
//                roomModel.getMemberInfoByUserIdOnlyLocal(eventInfo.second.elementAtOrNull(0))?.isInvitedInfoSeat =
//                    false
//                roomModel.noticeMemberListUpdate()
            }
            EVENT_AGREE_MANAGE_PICK -> {
                Log.d(TAG, "handleRoomEvent: EVENT_AGREE_MANAGE_PICK")
//                // 同意之后重置状态
//                roomModel.getMemberInfoByUserIdOnlyLocal(eventInfo.second.elementAtOrNull(0))?.isInvitedInfoSeat =
//                    false
//                roomModel.noticeMemberListUpdate()
            }
            EVENT_KICKED_OUT_OF_ROOM -> {
                if (eventInfo.second[1] == AccountStore.getUserId()) {
                    view.showMessage("你已被踢出房间")
                    leaveRoom()
                } else {
                    // TODO: 2021/7/2 显示被踢消息 
//                    sendSystemMessage("显示别人被踢消息")
                }
            }
            EVENT_REQUEST_SEAT_REFUSE -> {
                view.showMessage("您的上麦请求被拒绝")
                currentStatus = STATUS_NOT_ON_SEAT
                view.changeStatus(currentStatus)

            }
            EVENT_REQUEST_SEAT_AGREE -> {
                enterSeatIfAvailable()
                currentStatus = STATUS_NOT_ON_SEAT
                view.changeStatus(currentStatus)
            }
            EVENT_KICK_OUT_OF_SEAT -> {
                view.showMessage("您已被抱下麦位")
            }
            EVENT_REQUEST_SEAT_CANCEL -> {
                currentStatus = STATUS_NOT_ON_SEAT
                view.changeStatus(currentStatus)
            }

        }
    }

    private fun initRoomEventListener() {
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(roomModel)
        RCVoiceRoomEngine.getInstance().addMessageReceiveListener(this)
    }

    fun getCurrentRoomInfo(): UiRoomModel {
        return roomModel.currentUIRoomInfo
    }

    fun getCurrentSeatsInfo(): List<UiSeatModel> {
        return roomModel.currentUISeatInfoList
    }

    fun getMemberInfoByUserId(userId: String, block: (UiMemberModel?) -> Unit) {
        roomModel.queryUserInfoFromLocalAndServer(userId) {
            block.invoke(it)
        }
    }

    fun joinRoom() {
        Log.d(TAG, "joinRoom: ${roomId} isCreat:$isCreate roomId = $roomId")
        //先退上个房间
        leaveRoom(true)
        UIKit.postDelayed({
            if (isCreate) {
                currentRoomInfo?.roomBean?.let { roomBean ->
                    val info = RCVoiceRoomInfo().apply {
                        roomName = roomBean.roomName
                        seatCount = DefaultConfigConstant.DEFAULT_SEAT_COUNT
                        isFreeEnterSeat = false
                        isLockAll = false
                        isMuteAll = false
                    }
                    RCVoiceRoomEngine.getInstance()
                        .createAndJoinRoom(roomId, info, object : RCVoiceRoomCallback {
                            override fun onError(code: Int, message: String?) {
                                view.showError(code, message)
                                view.onJoinNextRoom(false)
                            }

                            override fun onSuccess() {
                                afterJoinRoomSuccess()
                            }
                        })
                }
            } else {
                RCVoiceRoomEngine.getInstance().joinRoom(roomId, object : RCVoiceRoomCallback {
                    override fun onError(code: Int, message: String?) {
                        view.showError(code, message)
                        view.onJoinNextRoom(false)
                    }

                    override fun onSuccess() {
                        afterJoinRoomSuccess()
                    }
                })
            }
        }, 300)
    }

    private fun afterJoinRoomSuccess() {
        view.onJoinRoomSuccess()
        afterInitView()
        roomModel.getOnLineUsersCount()
        roomModel.refreshAllMemberInfoList()
        sendSystemMessage()
        GlobalScope.launch(Dispatchers.IO) {
            AudioEffectManager.init()
        }
        AudioManagerUtil.choiceAudioModel()
    }

    private fun sendSystemMessage() {
        RCChatRoomMessageManager.sendLocationMessage(
            roomId,
            "欢迎来到 ${roomModel.currentUIRoomInfo.roomBean?.roomName}"
        )
        RCChatRoomMessageManager.sendLocationMessage(
            roomId,
            "感谢使用融云 RTC 语音房，请遵守相关法规，不要传播低俗、暴力等不良信息。欢迎您把使用过程中的感受反馈给我们。"
        )
        RCChatRoomMessageManager.sendChatMessage(
            roomId, RCChatroomEnter()
                .apply {
                    this.userId = AccountStore.getUserId()
                    this.userName = AccountStore.getUserName()
                }, false
        )
    }

    fun leaveRoom() {
        leaveRoom(false)
    }
    //离开房间
    private fun leaveRoom(joinNext: Boolean) {
        roomModel.onLeaveRoom()
        RCVoiceRoomEngine.getInstance().leaveRoom(object : RCVoiceRoomCallback {
            override fun onError(code: Int, message: String?) {
                view.showError(code, message)
                if (joinNext)
                    view.onJoinNextRoom(true)
                else
                    view.leaveRoomSuccess()
            }

            override fun onSuccess() {
                if (joinNext)
                    view.onJoinNextRoom(true)
                else
                    view.leaveRoomSuccess()
            }
        })
    }
    //关闭房间
    fun closeRoom() {
        view.showWaitingDialog()
        RCVoiceRoomEngine.getInstance().notifyVoiceRoom(EVENT_ROOM_CLOSE, "")
        VoiceRoomNetManager
            .aRoomApi
            .deleteRoom(roomId)
            .delay(2, TimeUnit.SECONDS)
            .subscribe({ result ->
                view.hideWaitingDialog()
                if (result.code == ApiConstant.REQUEST_SUCCESS_CODE) {
                    leaveRoom()
                } else {
                    view.showError(result.code, result.msg)
                }
            }, { t ->
                view.hideWaitingDialog()
                view.showError(-1, t.message)
            })
    }
    //收起房间，最小化
    fun packUpRoom(){
        view.packupRoom()
    }
    fun roomOwnerEnterSeat() {
        RCVoiceRoomEngine.getInstance().enterSeat(0, object : RCVoiceRoomCallback {
            override fun onError(code: Int, message: String?) {
                Log.d(TAG, "enterSeat::onError: $message")
                view.showError(code, message)
            }

            override fun onSuccess() {
                Log.d(TAG, "onSuccess: ")
                view.enterSeatSuccess()
                AudioManagerUtil.choiceAudioModel()
            }
        })
    }

    fun enterSeat(seatIndex: Int) {
        Log.d(TAG, "enterSeat: seatIndex = $seatIndex")
        // 当前在座位上
        if (roomModel.userInSeat(AccountStore.getUserId()!!)) {
            RCVoiceRoomEngine.getInstance().switchSeatTo(seatIndex, object : RCVoiceRoomCallback {
                override fun onError(code: Int, message: String?) {
                    Log.d(TAG, "enterSeat::onError: $message")
                    view.showError(code, message)
                }

                override fun onSuccess() {
                    Log.d(TAG, "onSuccess: ")
//                    view.enterSeatSuccess()
                    AudioManagerUtil.choiceAudioModel()
                }
            })
        } else {
            if (currentStatus == STATUS_WAIT_FOR_SEAT && !roomModel.currentUIRoomInfo.isFreeEnterSeat) {
                view.showRevokeSeatRequest()
                return
            }
            // 自由上麦模式
            if (roomModel.currentUIRoomInfo.isFreeEnterSeat) {
                var index = seatIndex
                if (index == -1) {
                    index = roomModel.getAvailableIndex()
                }
                if (index == -1) {
                    view.showError("当前麦位已满")
                    return
                }
                RCVoiceRoomEngine.getInstance().enterSeat(index, object : RCVoiceRoomCallback {
                    override fun onError(code: Int, message: String?) {
                        Log.d(TAG, "enterSeat::onError: $message")
                        view.showError(code, message)
                    }

                    override fun onSuccess() {
                        Log.d(TAG, "enterSeat:onSuccess: ")
                        view.enterSeatSuccess()
                        AudioManagerUtil.choiceAudioModel()
                    }
                })
            } else {
                roomModel.requestSeat {
                    if (it) {
                        currentStatus = STATUS_WAIT_FOR_SEAT
                        view.changeStatus(STATUS_WAIT_FOR_SEAT)
                        view.showMessage("已申请连线，等待房主接受")
                    } else {
                        view.showError("请求排麦失败")
                    }
                }

            }
        }
    }

    fun sendFovMessage() {
        RCChatRoomMessageManager.sendChatMessage(roomId, RCChatroomLike()
            .apply {
            },
            true,
            {
            }, { code, _ ->
                view.showError("发送失败：${code?.code}")
            })
    }

    fun sendMessage(message: String) {
        RCChatRoomMessageManager.sendChatMessage(roomId, RCChatroomBarrage()
            .apply {
                userId = AccountStore.getUserId()
                userName = AccountStore.getUserName()
                content = message
            },
            true,
            {
                view.sendTextMessageSuccess(message)
            }, { code, _ ->
                view.showError("发送失败：${code?.code}")
            })
    }

    override fun onReceived(message: Message?, left: Int): Boolean {
        Log.d(TAG, "onReceived: ")
        message?.content?.let {
            RCChatRoomMessageManager.onReceiveMessage(roomId, it)
        }
        return true
    }


    fun refuseInvite(userId: String) {
        RCVoiceRoomEngine.getInstance()
            .notifyVoiceRoom(EVENT_REJECT_MANAGE_PICK, AccountStore.getUserId())
    }

    fun enterSeatIfAvailable() {
        RCVoiceRoomEngine.getInstance()
            .notifyVoiceRoom(EVENT_AGREE_MANAGE_PICK, AccountStore.getUserId())
        val availableIndex = roomModel.getAvailableIndex()
        if (availableIndex > 0) {
            RCVoiceRoomEngine
                .getInstance()
                .enterSeat(availableIndex, object : RCVoiceRoomCallback {
                    override fun onError(code: Int, message: String?) {
                        view.showError(message)
                    }

                    override fun onSuccess() {
                        view.showMessage("上麦成功")
                        AudioManagerUtil.choiceAudioModel()
                    }

                })
        } else {
            view.showError("当前没有空余的麦位")
        }
    }
}