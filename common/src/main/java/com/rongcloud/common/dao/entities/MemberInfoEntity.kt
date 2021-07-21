/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author gusd
 * @Date 2021/07/21
 */
@Entity(tableName = "MemberInfo")
data class MemberInfoEntity(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "userName") val userName: String?,
    @ColumnInfo(name = "portrait") val portrait: String?,
    @ColumnInfo(name = "number") val number: String?
)
