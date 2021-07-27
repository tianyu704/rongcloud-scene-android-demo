/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.bean

import javax.lang.model.element.Element

/**
 * @author gusd
 * @Date 2021/07/27
 */
data class HiltBindingBean(
    var clazz: Class<*>,
    var viewClazz: Class<*>,
    var typeEnum: TypeEnum,
    var element:Element
)

enum class TypeEnum {
    ACTIVITY,
    FRAGMENT,
    VIEW
}