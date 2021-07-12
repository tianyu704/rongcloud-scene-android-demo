/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.model

import android.content.Context
import android.os.Environment
import com.example.voiceroomdemo.MyApp
import com.example.voiceroomdemo.common.showToast
import com.example.voiceroomdemo.net.RetrofitManager
import com.example.voiceroomdemo.utils.FileUtil
import io.reactivex.rxjava3.core.Completable
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

private const val TAG = "FileModel"

object FileModel {
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
                    .map { shortUrl ->
                        return@map "${shortUrl.data}"
                    }
            }
    }

    private var isDownloading = false

    fun downloadMusic(
        context: Context,
        url: String,
        fileName: String,
        block: ((Long, Long) -> Unit)? = null
    ): Completable {
        if (isDownloading) {
            // 正在下载中
            context.showToast("正在下载中")
            return Completable.never()
        }
        isDownloading = true
        return Completable.create { emitter ->
            RetrofitManager
                .downloadService
                .downloadFile(url)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    FileUtil.writeFile(
                        it.byteStream(),
                        getCompleteMusicPathByName(fileName)
                    ) { progress ->
                        block?.invoke(it.contentLength(), progress)
                    }
                }.subscribe({

                }, {
                    isDownloading = false
                    emitter.onError(it)
                }, {
                    isDownloading = false
                    emitter.onComplete()
                })
        }

    }

    fun getNameFromUrl(url: String): String? {
        val split = url.split("/")
        return split.lastOrNull()?.replace(" ", "_")
    }

    fun checkOrDownLoadMusic(
        url: String,
        block: ((Long, Long) -> Unit)? = null
    ): Completable {
        return Completable.create { emitter ->
            if (!FileUtil.exists(getCompleteMusicPathByName(getNameFromUrl(url) ?: ""))) {
                this@FileModel.downloadMusic(MyApp.context, url, getNameFromUrl(url) ?: "", block)
                    .subscribe({
                        emitter.onComplete()
                    }, {
                        emitter.onError(it)
                    })
            } else {
                emitter.onComplete()
            }
        }

    }

    fun getCompleteMusicPathByName(name: String): String {
        return "${MyApp.context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}${File.separator}${
            name.replace(" ", "_")
        }"
    }

}