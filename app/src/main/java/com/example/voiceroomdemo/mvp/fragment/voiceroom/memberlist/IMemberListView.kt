/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.memberlist

import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting.IMemberSettingView
import com.example.voiceroomdemo.net.api.bean.respond.Member
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel

/**
 * @author gusd
 * @Date 2021/06/21
 */
interface IMemberListView :IBaseView{
    fun showMemberList(data: List<UiMemberModel>?){}
}