/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.presenter

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import cn.rongcloud.voiceroom.net.bean.respond.VoiceRoomBean
import cn.rongcloud.voiceroom.pk.TestPkActivity
import cn.rongcloud.voiceroom2.ScorlVoiceRoomActivity
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.IVoiceRoomListView
import cn.rongcloud.voiceroomdemo.mvp.model.EMPTY_ROOM_INFO
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomListModel
import com.kit.cache.GsonUtil
import com.rongcloud.common.base.BaseLifeCyclePresenter
import com.rongcloud.common.utils.AccountStore
import com.rongcloud.common.utils.UIKit
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/09
 */
@ActivityScoped
class VoiceRoomListPresenter @Inject constructor(
    val view: IVoiceRoomListView,
    private val voiceRoomListMode: VoiceRoomListModel,
    activity: AppCompatActivity
) :
    BaseLifeCyclePresenter(activity) {


    override fun onCreate() {
        super.onCreate()
        initObDataChange()
    }

    private fun initObDataChange() {
        addDisposable(
            voiceRoomListMode
                .obVoiceRoomList()
                .subscribe { bean ->
                    view.onDataChange(bean)
                }
        )

        addDisposable(voiceRoomListMode
            .obVoiceRoomErrorEvent()
            .subscribe {
                view.onLoadError(it)
            })
    }

    override fun onResume() {
        super.onResume()
        voiceRoomListMode.refreshDataList()
    }

    fun refreshData() {
        voiceRoomListMode.refreshDataList()
    }

    fun loadMore() {
        voiceRoomListMode.loadMoreData()
    }

    fun gotoVoiceRoomActivity(
        context: Context,
        roomId: String,
        isCreate: Boolean = false,
        list: List<VoiceRoomBean>
    ) {
        if (isCreate) {

            ScorlVoiceRoomActivity.startActivity(context, roomId, list, isCreate)
            return
        }
        view.showWaitingDialog()
        voiceRoomListMode
            .queryRoomInfoFromServer(roomId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { view.hideWaitingDialog() }
            .doFinally {
                view.hideWaitingDialog()
            }
            .subscribe({ info ->
                if (info.room == null || info.room == EMPTY_ROOM_INFO) {
                    view.showError("房间不存在")
                } else {
                    if (info.room?.isPrivate == 1 && info.room?.createUser?.userId != AccountStore.getUserId()) {
                        view.showInputPasswordDialog(info.room!!)
                    } else {
                        turnToRoom(context, info.room, list, isCreate)
                    }
                }
            }, { t ->
                view.showError(t.message)
            })

    }

    fun turnToRoom(
        context: Context,
        info: VoiceRoomBean?,
        list: List<VoiceRoomBean>,
        isCreate: Boolean
    ) {
        info?.createUser?.let {
//            ScorlVoiceRoomActivity.startActivity(context, info.roomId, Arrays.asList(info), isCreate)
            UIKit.startActivityByBasis(
                context as Activity,
                TestPkActivity::class.java,
                GsonUtil.obj2Json(info)
            )
        } ?: view.showError("房间数据错误")
    }

    fun addRoomInfo(roomInfo: VoiceRoomBean) {
        voiceRoomListMode.addRoomInfo(roomInfo)
    }
}