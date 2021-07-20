/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting

import cn.rongcloud.voiceroomdemo.common.BaseLifeCyclePresenter
import cn.rongcloud.voiceroomdemo.common.LocalDataStore
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/22
 */
class BackgroundSettingPresenter @Inject constructor(val view: IBackgroundSettingView) :
    BaseLifeCyclePresenter<IBackgroundSettingView>(view) {

    override fun onResume() {
        super.onResume()
        view.onBackgroundList(LocalDataStore.getBackGroundUrlList())
    }
}