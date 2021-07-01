/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.createroom

import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean

/**
 * @author gusd
 * @Date 2021/06/15
 */
interface ICreateVoiceRoomView:IBaseView {
    fun onCreateRoomSuccess(data: VoiceRoomBean?)
    fun onCreateRoomExist(data: VoiceRoomBean?)

}