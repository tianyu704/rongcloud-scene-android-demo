/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.di

import android.app.Activity
import cn.rongcloud.voiceroomdemo.mvp.activity.VoiceRoomActivity
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.IHomeView
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.ILoginView
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.IVoiceRoomListView
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.IVoiceRoomView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named

/**
 * @author gusd
 * @Date 2021/07/20
 */
@Module
@InstallIn(ActivityComponent::class)
public class HiltActivityModule {

    @Provides
    fun provideLoginView(loginView: Activity): ILoginView = loginView as ILoginView

    @Provides
    fun provideHomeView(homeView: Activity): IHomeView = homeView as IHomeView

    @Provides
    fun provideVoiceRoomListView(voiceRoomListView: Activity) =
        voiceRoomListView as IVoiceRoomListView

    @Provides
    fun provideVoiceRoomView(voiceRoomView: Activity) = voiceRoomView as IVoiceRoomView

    @Named("roomId")
    @Provides
    fun provideVoiceRoomId(activity: Activity): String {
        if (activity is VoiceRoomActivity) {
            return activity.getRoomId()
        }
        return ""
    }
}