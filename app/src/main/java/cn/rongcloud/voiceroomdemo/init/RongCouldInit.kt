/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.init

import android.app.Application
import android.content.Context
import cn.rongcloud.annotation.AutoInit
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback
import cn.rongcloud.voiceroomdemo.MyApp
import cn.rongcloud.voiceroomdemo.utils.RCChatRoomMessageManager
import com.rongcloud.common.extension.showToast
import com.rongcloud.common.init.ModuleInit
import com.rongcloud.common.utils.AccountStore
import io.rong.imlib.RongIMClient
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/08/03
 * 融云相关业务的初始化
 */
@AutoInit
class RongCouldInit @Inject constructor() : ModuleInit {
    override fun getPriority(): Int {
        return 100
    }

    override fun getName(context: Context): String = "RongCloudInit"

    override fun onInit(application: Application) {
        RCVoiceRoomEngine.getInstance().initWithAppKey(application, MyApp.APP_KEY)
        RCChatRoomMessageManager.registerMessageTypes()

        RongIMClient.setConnectionStatusListener { status ->
            if (status == RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                application.showToast("当前账号已在其他设备登录，请重新登录")
                AccountStore.logout()
            }
        }

        if (!AccountStore.getImToken().isNullOrEmpty()) {
            RCVoiceRoomEngine.getInstance().connectWithToken(
                MyApp.context as Application,
                AccountStore.getImToken(),
                object : RCVoiceRoomCallback {
                    override fun onError(code: Int, message: String?) {
                        MyApp.context.showToast("RTC 服务器连接失败,请重新登录")
                        AccountStore.logout()
                    }

                    override fun onSuccess() {

                    }

                })
        }

    }


}