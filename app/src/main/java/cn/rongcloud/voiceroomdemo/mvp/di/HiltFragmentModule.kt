/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.di

import android.app.Activity
import androidx.fragment.app.Fragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.createroom.ICreateVoiceRoomView
import cn.rongcloud.voiceroomdemo.mvp.fragment.present.ISendPresentView
import cn.rongcloud.voiceroomdemo.mvp.fragment.present.SendPresentFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting.ICreatorView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting.EmptySeatFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting.IEmptySeatView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.memberlist.IMemberListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.membersetting.IMemberSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.membersetting.MemberSettingFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.IMusicAddView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.IMusicControlView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.IMusicListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting.IMusicSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest.IRevokeSeatView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting.IBackgroundSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.IRoomSettingView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.IInviteSeatListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.IRequestSeatListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation.IViewPageListView
import cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.selfsetting.ISelfSettingView
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import cn.rongcloud.voiceroomdemo.ui.uimodel.UiMemberModel
import cn.rongcloud.voiceroomdemo.ui.uimodel.UiSeatModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import javax.inject.Named

/**
 * @author gusd
 * @Date 2021/07/20
 */
@Module
@InstallIn(FragmentComponent::class)
class HiltFragmentModule {


    @Provides
    fun provideCreateVoiceRoomView(fragment: Fragment) = fragment as ICreateVoiceRoomView

    @Provides
    fun provideISendPresentView(fragment: Fragment) = fragment as ISendPresentView

    @Provides
    fun provideICreatorView(fragment: Fragment) = fragment as ICreatorView

    @Provides
    fun provideIEmptySeatView(fragment: Fragment) = fragment as IEmptySeatView

    @Named("EmptySeatSetting")
    @Provides
    fun provideEmptySeatSettingBean(fragment: Fragment): UiSeatModel {
        return (fragment as EmptySeatFragment).seatInfo
    }

    @Named("SelfSeatSetting")
    @Provides
    fun provideSelfSeatSettingBean(fragment: Fragment): UiSeatModel {
        return (fragment as EmptySeatFragment).seatInfo
    }

    @Provides
    fun provideIMemberListView(fragment: Fragment) = fragment as IMemberListView

    @Provides
    fun provideIMemberSettingView(fragment: Fragment) = fragment as IMemberSettingView

    @Provides
    fun provideRoomInfoBean(roomModel: VoiceRoomModel): VoiceRoomBean {
        return roomModel.currentUIRoomInfo.roomBean!!
    }

    @Provides
    fun provideMemberModel(fragment: Fragment): UiMemberModel {
        return (fragment as MemberSettingFragment).member
    }

    @Provides
    fun provideIMusicSettingView(fragment: Fragment) = fragment as IMusicSettingView

    @Provides
    fun provideIRevokeSeatView(fragment: Fragment) = fragment as IRevokeSeatView

    @Provides
    fun provideIBackgroundSettingView(fragment: Fragment) = fragment as IBackgroundSettingView

    @Provides
    fun provideIRoomSettingView(fragment: Fragment) = fragment as IRoomSettingView

    @Provides
    fun provideIViewPageListView(fragment: Fragment) = fragment as IViewPageListView

    @Provides
    fun provideISelfSettingView(fragment: Fragment) = fragment as ISelfSettingView

    @Provides
    fun provideIInviteSeatListView(fragment: Fragment) = fragment as IInviteSeatListView

    @Provides
    fun provideIRequestSeatListView(fragment: Fragment) = fragment as IRequestSeatListView

    @Provides
    fun provideIMusicListView(fragment: Fragment) = fragment as IMusicListView

    @Provides
    fun provideIMusicControlView(fragment: Fragment) = fragment as IMusicControlView

    @Provides
    fun provideIMusicAddView(fragment: Fragment) = fragment as IMusicAddView

    @Provides
    @Named("selectedIds")
    fun provideSelectedIds(activity: Activity, fragment: Fragment): List<String> {
        if (fragment is SendPresentFragment) {
            fragment.getSelectedIds()
        }
        return emptyList()
    }
}