/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.annotation

import kotlin.reflect.KClass

/**
 * @author gusd
 * @Date 2021/07/27
 * @Description 用于绑定 View 和实现类，实现类必须集成自 Activity，Fragment 或者 View
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class HiltBinding(val value: KClass<*> = Void::class)
