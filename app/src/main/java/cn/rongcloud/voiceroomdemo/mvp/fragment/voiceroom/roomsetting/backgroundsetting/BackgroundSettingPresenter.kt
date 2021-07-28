/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting

import androidx.fragment.app.Fragment
import cn.rongcloud.voiceroomdemo.common.LocalDataStore
import com.rongcloud.common.base.BaseLifeCyclePresenter
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/22
 */
class BackgroundSettingPresenter @Inject constructor(
    val view: IBackgroundSettingView,
    fragment: Fragment
) :
    BaseLifeCyclePresenter(fragment) {

    override fun onResume() {
        super.onResume()
        view.onBackgroundList(LocalDataStore.getBackGroundUrlList())
    }
}