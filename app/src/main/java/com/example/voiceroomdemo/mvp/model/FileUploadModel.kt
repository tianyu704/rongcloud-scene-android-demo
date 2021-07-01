/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.model

import android.content.Context
import com.example.voiceroomdemo.net.RetrofitManager
import com.example.voiceroomdemo.net.api.ApiConstant
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import top.zibin.luban.Luban
import java.io.File

/**
 * @author gusd
 * @Date 2021/06/15
 */
object FileUploadModel {
    fun imageUpload(imagePath: String, context: Context): Single<String> {
        return Flowable
            .just(imagePath)
            .observeOn(Schedulers.io())
            .map {
                return@map Luban.with(context).ignoreBy(100).setFocusAlpha(true).load(it).get()[0]
            }
            .first(File(imagePath))
            .flatMap {
                val requestBody = RequestBody.create(MediaType.parse("image/*"), it)
                val part = MultipartBody.Part.createFormData("file", it.name, requestBody)
                return@flatMap RetrofitManager
                    .commonService
                    .fileUpload(part)
                    .map { shortUrl -> return@map "${shortUrl.data}" }
            }
    }
}