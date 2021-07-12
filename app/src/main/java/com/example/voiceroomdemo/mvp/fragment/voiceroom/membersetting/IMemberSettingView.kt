/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting

import com.example.voiceroomdemo.common.IBaseView

/**
 * @author gusd
 * @Date 2021/06/21
 */
interface IMemberSettingView:IBaseView {
    fun loginUserIsCreator(isCreatorUser: Boolean, isAdmin: Boolean){}
    fun thisUserIsOnSeat(seatIndex: Int,isAdmin:Boolean){}
    fun thisUserIsAdmin(isAdmin: Boolean){}
    fun thisUserIsMute(isMute:Boolean){}
    fun fragmentDismiss() {}
    fun sendGift(userId: String){}
}