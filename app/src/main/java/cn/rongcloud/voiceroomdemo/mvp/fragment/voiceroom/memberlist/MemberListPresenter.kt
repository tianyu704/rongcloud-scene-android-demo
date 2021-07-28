/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.memberlist

import android.util.Log
import com.rongcloud.common.base.BaseLifeCyclePresenter
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/21
 */
private const val TAG = "MemberListPresenter"

class MemberListPresenter @Inject constructor(
    private val view: IMemberListView,
    private val roomModel: VoiceRoomModel
) : BaseLifeCyclePresenter<IMemberListView>(view) {

    fun getMemberList() {
        addDisposable(
            roomModel
                .obMemberListChange()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ bean ->
                    Log.d(TAG, "getMemberList: ${bean.size}")
                    view.showMemberList(bean)
                }, { t ->
                    view.showError(-1, t.message)
                })
        )


    }

    override fun onResume() {
        super.onResume()
        roomModel.refreshAllMemberInfoList()
    }
}