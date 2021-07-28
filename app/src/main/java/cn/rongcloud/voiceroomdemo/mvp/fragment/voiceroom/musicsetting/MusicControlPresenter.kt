/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.rongcloud.common.base.BaseLifeCyclePresenter
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/07/06
 */
class MusicControlPresenter @Inject constructor(view: IMusicControlView) :
    BaseLifeCyclePresenter<IMusicControlView>(view)