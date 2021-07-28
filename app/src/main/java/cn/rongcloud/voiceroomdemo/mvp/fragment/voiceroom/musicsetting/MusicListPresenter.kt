/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.rongcloud.common.base.BaseLifeCyclePresenter
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import cn.rongcloud.voiceroomdemo.ui.uimodel.UiMusicModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/07/06
 */
class MusicListPresenter @Inject constructor(val view: IMusicListView, val roomModel: VoiceRoomModel) :
    BaseLifeCyclePresenter<IMusicListView>(view) {

    override fun onStart() {
        super.onStart()
        addDisposable(
            roomModel.obUserMusicListChange()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showMusicList(it)
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun deleteMusic(model: UiMusicModel) {
        model.id?.let {
            addDisposable(
                roomModel.deleteMusic(model.url ?: "", it)
                    .subscribe({

                    }, {
                        view.showError(it.message)
                    })
            )
        }

    }

    fun playOrPauseMusic(model: UiMusicModel) {
        roomModel.playOrPauseMusic(model)
    }

    fun moveMusicTop(model: UiMusicModel) {
        addDisposable(roomModel.moveMusicToTop(model).subscribe({

        }, {
            view.showError(it.message)
        }))
    }
}