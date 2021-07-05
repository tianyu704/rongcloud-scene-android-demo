/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.utils

import android.content.Context
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.getCompletePortraitUrl
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * @author gusd
 * @Date 2021/06/08
 */
@ExperimentalCoroutinesApi
object ImageLoaderUtil {
    fun loadImage(
        context: Context,
        imageView: ImageView,
        url: String,
        @DrawableRes resId: Int = 0
    ) {
        if (url.isEmpty()) {
            Glide.with(context).load(resId).into(imageView)
        } else {
            Glide.with(context).load(url).error(resId).placeholder(resId).centerCrop()
                .into(imageView)
        }
    }

    fun loadPortrait(context: Context, imageView: ImageView, url: String) {
        val realUrl = url.getCompletePortraitUrl() ?: ""
        Glide.with(context).load(realUrl).error(R.drawable.default_portrait)
            .placeholder(R.drawable.default_portrait).centerCrop().into(imageView)
    }

    fun loadImage(context: Context, imageView: ImageView, @DrawableRes drawableId: Int) {
        Glide.with(context).load(drawableId).into(imageView)
    }

    fun loadLocalImage(context: Context, imageView: ImageView, localPath: String) {
        Glide.with(context).load(localPath).into(imageView)
    }


}