/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.init

import android.app.Application
import android.content.Context
import cn.rongcloud.annotation.AutoInit
import com.rongcloud.common.extension.showToast
import com.rongcloud.common.init.ModuleInit
import com.rongcloud.common.utils.AccountStore
import io.rong.imlib.RongIMClient
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/08/03
 * 融云相关基础业务模块
 */
@AutoInit
class RongCouldInit @Inject constructor() : ModuleInit {
    override fun getPriority(): Int {
        return 100
    }

    override fun getName(context: Context): String = "RongCloudInit"

    override fun onInit(application: Application) {


        RongIMClient.setConnectionStatusListener { status ->
            if (status == RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                application.showToast("当前账号已在其他设备登录，请重新登录")
                AccountStore.logout()
            }
        }

    }


}