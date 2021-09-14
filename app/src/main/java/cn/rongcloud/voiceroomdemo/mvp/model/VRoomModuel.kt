///*
// * Copyright © 2021 RongCloud. All rights reserved.
// */
//
//package cn.rongcloud.voiceroomdemo.mvp.model
//
//import android.util.Log
//import androidx.fragment.app.Fragment
//import cn.rongcloud.rtc.api.RCRTCAudioMixer
//import cn.rongcloud.rtc.api.callback.RCRTCAudioMixingStateChangeListener
//import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
//import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomEventListener
//import cn.rongcloud.voiceroom.message.*
//import cn.rongcloud.voiceroom.model.*
//import cn.rongcloud.voiceroom.net.VoiceRoomNetManager
//import cn.rongcloud.voiceroom.net.bean.request.*
//import cn.rongcloud.voiceroom.ui.uimodel.*
//import cn.rongcloud.voiceroomdemo.MyApp
//import cn.rongcloud.voiceroomdemo.net.api.bean.request.*
//import com.rongcloud.common.base.BaseLifeCycleModel
//import com.rongcloud.common.extension.showToast
//import com.rongcloud.common.net.ApiConstant
//import com.rongcloud.common.utils.AccountStore
//import dagger.hilt.android.scopes.FragmentScoped
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
//import io.reactivex.rxjava3.core.*
//import io.reactivex.rxjava3.functions.BiFunction
//import io.reactivex.rxjava3.schedulers.Schedulers
//import kotlinx.coroutines.*
//import javax.inject.Inject
//import javax.inject.Named
//
///**
// * @author gusd
// * @Date 2021/06/18
// */
//
//
//@FragmentScoped
//class VRoomModel @Inject constructor(
//    @Named("roomId") val roomId: String,
//    private val voiceRoomListModel: VoiceRoomListModel,
//    fragment: Fragment
//) : BaseLifeCycleModel(fragment), RCVoiceRoomEventListener {
//    val vRoomWrapper: VRoomWrapper
//    val netApi: VRNetApi
//    init {
//        vRoomWrapper = VRoomWrapper(fragment.activity, roomId);
//        netApi = VRNetApi(vRoomWrapper)
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        //混音
//        RCRTCAudioMixer.getInstance().setAudioMixingStateChangeListener(object :
//            RCRTCAudioMixingStateChangeListener() {
//            override fun onMixEnd() {
//                Log.d(TAG, "onMixEnd: ")
//                playNextMusic()
//            }
//
//            override fun onStateChanged(
//                p0: RCRTCAudioMixer.MixingState,
//                p1: RCRTCAudioMixer.MixingStateReason?
//            ) {
//                Log.d(TAG, "onStateChanged: $p0")
//                currentMusicState = p0
//                when (p0) {
//                    RCRTCAudioMixer.MixingState.PLAY -> {
//                        vRoomWrapper.musics.forEach {
//                            it.isPlaying = currentPlayMusic == it.url
//                        }
//                    }
//                    RCRTCAudioMixer.MixingState.PAUSED -> {
//                        vRoomWrapper.musics.forEach {
//                            it.isPlaying = false
//                        }
//                    }
//                    RCRTCAudioMixer.MixingState.STOPPED -> {
//                        vRoomWrapper.musics.forEach {
//                            it.isPlaying = false
//                        }
//                    }
//                }
//                // TODO: 2021/9/14 music
////                userMusicListSubject.onNext(userMusicList)
//            }
//
//            override fun onReportPlayingProgress(p0: Float) {
//            }
//        })
//    }
//
//
//    override fun onDestroy() {
//        RCRTCAudioMixer.getInstance().setAudioMixingStateChangeListener(null)
//    }
//
//
//
//    fun setRecordingEnable(enable: Boolean): Completable {
//        return Completable.create { emitter ->
//            RCVoiceRoomEngine.getInstance().disableAudioRecording(!enable)
//            recordingStatus = enable
//            emitter.onComplete()
//        }
//    }
//
//    fun noticeMemberListUpdate() {
//        memberListChangeSubject.onNext(roomMemberInfoList)
//    }
//
//    private fun querySystemMusicList(): Single<List<UiMusicModel>> {
//        return queryMusicListByType(MUSIC_TYPE_SYSTEM)
//    }
//
//    private fun queryCustomizeMusicList(): Single<List<UiMusicModel>> {
//        return queryMusicListByType(MUSIC_FROM_TYPE_LOCAL)
//    }
//
//    private fun queryMusicListByType(type: Int): Single<List<UiMusicModel>> {
//        return VoiceRoomNetManager
//            .musicService
//            .getMusicList(MusicListRequest(roomId, type))
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .map {
//                return@map it.data?.map { bean -> UiMusicModel.create(bean) } ?: emptyList()
//            }
//    }
//
//
//    fun refreshMusicList(onComplete: (() -> Unit)? = null) {
//        addDisposable(
//            Observable.combineLatest(
//                querySystemMusicList().toObservable(),
//                queryCustomizeMusicList().toObservable(),
//                BiFunction { systemList, customList ->
//                    systemList.forEach { sysModel ->
//                        sysModel.addAlready = customList.firstOrNull {
//                            sysModel.url == it.url
//                        } != null
//                    }
//                    userMusicList.clear()
//                    userMusicList.addAll(customList)
//                    if (currentMusicState == RCRTCAudioMixer.MixingState.PLAY) {
//                        userMusicList.forEach {
//                            it.isPlaying = currentPlayMusic == it.url
//                        }
//                    }
//
//                    systemMusicList.clear()
//                    systemMusicList.addAll(systemList)
//                    userMusicListSubject.onNext(customList)
//                    systemMusicListSubject.onNext(systemList)
//                    return@BiFunction emptyList<UiMusicModel>()
//                }).subscribe {
//                onComplete?.invoke()
//            }
//        )
//    }
//
//    fun addMusic(
//        name: String,
//        author: String? = "",
//        type: Int = 0,
//        url: String,
//        size: Long? = null
//    ): Completable {
//        Log.d(TAG, "addMusic: name = $name,author = $author,type = $type,url = $url")
//        return Completable.create { emitter ->
//            addDisposable(
//                VoiceRoomNetManager
//                    .musicService
//                    .addMusic(
//                        AddMusicRequest(
//                            name = name,
//                            author = author,
//                            roomId = roomId,
//                            type = type,
//                            url = url,
//                            size = size
//                        )
//                    ).subscribe({ result ->
//                        if (result.code == ApiConstant.REQUEST_SUCCESS_CODE) {
//                            refreshMusicList {
//                                if (userMusicList.size == 1) {
//                                    Log.d(TAG, "addMusic: list only one music,start play")
//                                    userMusicList.elementAtOrNull(0)?.url?.let {
//                                        playMusic(name, it)
//                                    }
//                                }
//                            }
//                            emitter.onComplete()
//                        } else {
//                            emitter.onError(Throwable(result.msg))
//                        }
//                    }, {
//                        emitter.onError(it)
//                    })
//            )
//        }
//
//    }
//
//    fun deleteMusic(url: String, id: Int): Completable {
//        return Completable.create { emitter ->
//            VoiceRoomNetManager
//                .musicService
//                .musicDelete(DeleteMusicRequest(id, roomId))
//                .subscribe({
//                    if (it.code == ApiConstant.REQUEST_SUCCESS_CODE) {
//                        if (url == currentPlayMusic) {
//
//                            try {
//                                stopPlayMusic()
//                            } catch (e: Exception) {
//                                Log.e(TAG, "deleteMusic: ", e)
//                            }
//                        }
//                        refreshMusicList()
//                        emitter.onComplete()
//                    } else {
//                        emitter.onError(Throwable(it.msg))
//                    }
//                }, {
//                    emitter.onError(it)
//                })
//        }
//    }
//
//    private var currentPlayMusic: String? = null
//    private var currentMusicState = RCRTCAudioMixer.MixingState.STOPPED
//
//
//    fun playOrPauseMusic(model: UiMusicModel) {
//        if (getSeatInfoByUserId(AccountStore.getUserId()) == null) {
//            MyApp.context.showToast("请先上麦之后再播放音乐")
//            return
//        }
//        playNextMusicJob?.let {
//            if (it.isActive) {
//                it.cancel()
//            }
//        }
//
//        if (currentPlayMusic.isNullOrEmpty()) {
//            // 当前没在播放，直接播放
//            model.url?.let { playMusic(model.name, it) }
//        } else if (currentPlayMusic != model.url) {
//            // 当前在播放的和选择的不同,停止播放旧的，直接播放新的
//            stopPlayMusic()
//            model.url?.let { playMusic(model.name, it) }
//        } else {
//            // 暂停
//            if (currentMusicState == RCRTCAudioMixer.MixingState.PAUSED) {
//                RCRTCAudioMixer.getInstance().resume()
//            } else if (currentMusicState == RCRTCAudioMixer.MixingState.PLAY) {
//                RCRTCAudioMixer.getInstance().pause()
//            }
//        }
//    }
//
//    private fun playMusic(name: String? = "", url: String) {
//        musicStopFlag = false
//        GlobalScope.launch(Dispatchers.IO) {
//            addDisposable(
//                FileModel
//                    .checkOrDownLoadMusic(name ?: "", url)
//                    .subscribe({
//                        if (musicStopFlag) {
//                            return@subscribe
//                        }
//                        currentPlayMusic = url
//                        val path = FileModel.getCompleteMusicPathByName(
//                            FileModel.getNameFromUrl(url) ?: ""
//                        )
//                        Log.d(TAG, "playMusic: path = $path")
//                        RCRTCAudioMixer.getInstance()
//                            .startMix(path, RCRTCAudioMixer.Mode.MIX, true, 1)
//                    }, {
//                        MyApp.context.showToast(it.message)
//                    })
//            )
//        }
//    }
//
//    fun moveMusicToTop(model: UiMusicModel): Completable {
//        return Completable.create { emitter ->
//
//            if (currentPlayMusic.isNullOrEmpty() || currentMusicState == RCRTCAudioMixer.MixingState.PAUSED) {
//                model.url?.let {
//                    stopPlayMusic()
//                    playMusic(model.name, it)
//                    emitter.onComplete()
//                }
//            } else {
//                val currentMusic = userMusicList.lastOrNull {
//                    it.url == currentPlayMusic
//                }
//                VoiceRoomNetManager
//                    .musicService
//                    .modifyMusicOrder(
//                        MusicOrderRequest(roomId, model.id, currentMusic?.id ?: 0)
//                    ).subscribe({
//                        if (it.code == ApiConstant.REQUEST_SUCCESS_CODE) {
//                            refreshMusicList()
//                            emitter.onComplete()
//                        } else {
//                            emitter.onError(Throwable(it.msg))
//                        }
//                    }, {
//                        emitter.onError(it)
//                    })
//            }
//        }
//    }
//
//    private var playNextMusicJob: Job? = null
//    private fun playNextMusic() {
//        Log.d(TAG, "playNextMusic: ")
//        if (playNextMusicJob?.isActive == true) {
//            playNextMusicJob?.cancel()
//        }
//        playNextMusicJob = GlobalScope.launch(Dispatchers.IO) {
//            // FIXME: 2021/7/12 sdk 混音存在问题，添加延迟临时处理
//            val index = userMusicList.indexOfLast {
//                it.url == currentPlayMusic
//            }
//            Log.d(TAG, "playNextMusic: index = $index")
//            currentPlayMusic = null
//            // 及时通知上层音乐播放完成
//            userMusicListSubject.onNext(userMusicList)
//            userMusicList
//                .elementAtOrNull((index + 1) % userMusicList.size)?.let {
//                    Log.d(TAG, "playNextMusic: $it")
//                    delay(1000)
//                    if (musicStopFlag) {
//                        return@launch
//                    }
//                    it.url?.let { it1 -> playMusic(it.name, it1) }
//                } ?: run {
//                Log.d(TAG, "playNextMusic: not find next music,try play first index music")
//                if (userMusicList.size > 0) {
//                    userMusicList[0].let {
//                        delay(1000)
//                        if (musicStopFlag) {
//                            return@launch
//                        }
//                        it.url?.let { it1 -> playMusic(it.name, it1) }
//                    }
//                }
//            }
//        }
//    }
//
//    fun onLeaveRoom() {
//        if (currentMusicState == RCRTCAudioMixer.MixingState.PAUSED
//            || currentMusicState == RCRTCAudioMixer.MixingState.PLAY
//        ) {
//            stopPlayMusic()
//        }
//    }
//
//    fun stopPlayMusic() {
//        try {
//            musicStopFlag = true
//            if (playNextMusicJob?.isActive == true) {
//                playNextMusicJob?.cancel()
//            }
//            RCRTCAudioMixer.getInstance().stop()
//        } catch (e: Exception) {
//            Log.e(TAG, "stopPlayMusic: ", e)
//        }
//    }
//
//    fun isPlayingMusic(): Boolean {
//        // 暂停状态下不视为音乐正在播放
//        return (currentPlayMusic != null && currentMusicState == RCRTCAudioMixer.MixingState.PLAY)
//                || (playNextMusicJob?.isCompleted == false)
//    }
//
//    /**
//     * 用于记录音乐停止的状态
//     */
//    @Volatile
//    private var musicStopFlag = true
//
//}