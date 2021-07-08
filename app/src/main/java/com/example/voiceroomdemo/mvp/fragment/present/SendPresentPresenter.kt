/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.present

import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.Present
import com.example.voiceroomdemo.mvp.model.VoiceRoomModel
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * @author gusd
 * @Date 2021/06/28
 */

class SendPresentPresenter(val view: ISendPresentView, roomId: String) :
    BaseLifeCyclePresenter<ISendPresentView>(view) {

    private val roomModel: VoiceRoomModel by lazy {
        getVoiceRoomModelByRoomId(roomId)
    }

    fun initeialObserve() {
        view.onPresentInited(roomModel.presents)
        addDisposable(roomModel
            .obMemberListChange()
            .map {
                return@map it.filter {
                    it.userId != AccountStore.getUserId() && it.seatIndex >= 0
                }
                return@map it
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                view.onMemberModify(it)
                allCount = it.size;
            })
    }

    // 存储选中发生对象
    private val selects: ArrayList<UiMemberModel> by lazy {
        return@lazy ArrayList<UiMemberModel>(16)
    }

    var currentPresent: Present? = null
        set(value) {
            if (field != value) {
                field = value
                presentNum = 1
            }
            checkEnableSend()
        }

    // 注意：新曾选中时 会将num重置
    var presentNum: Int = 1

    /**
     * 跟新则发送礼物的成员的选择状态
     * @return selected 选中状态 true： 当前选中  false：当前未选中
     */
    fun updateSelected(member: UiMemberModel): Boolean {
        selects.let {
            if (selects.contains(member)) {
                selects.remove(member)
                checkEnableSend()
                return false
            } else {
                selects.add(member)
                presentNum = 1
                checkEnableSend()
                return true
            }
        }
        return false
    }

    fun isSelected(member: UiMemberModel): Boolean {
        selects.let {
            return it.contains(member)
        }
    }

    var allCount: Int = 0;
    fun selectAll(members: List<UiMemberModel>?) {
        selects.let {
            selects.clear();
            members?.let {
                selects.addAll(it)
            }
        }
        presentNum = 1
        checkEnableSend()
    }

    /**
     * 检查赠送状态
     */
    fun checkEnableSend() {
        val enable = null != currentPresent && selects.isNotEmpty();
        view.onEnableSend(enable)
    }

    fun sendPresent() {
        view.showMessage("赠送礼物：数量: $presentNum 人数:${selects.size} 礼物索引:${currentPresent?.index ?: 0} 价值：${currentPresent?.price}")
        currentPresent?.let { present ->
            addDisposable(
                roomModel
                    .sendGift(selects, present, presentNum)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { members ->
                        roomModel.sendGiftMsg(members, present, presentNum)
                    }
            )
        }

    }

    override fun onResume() {
        super.onResume()
        roomModel.refreshAllMemberInfoList()
    }
}

