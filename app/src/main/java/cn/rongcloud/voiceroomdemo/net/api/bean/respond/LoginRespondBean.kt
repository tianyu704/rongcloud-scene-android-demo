/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.net.api.bean.respond

import com.rongcloud.common.model.AccountInfo

/**
 * @author gusd
 * @Date 2021/06/07
 */
data class LoginRespondBean(val code: Int,val msg: String?, val data: AccountInfo?)




