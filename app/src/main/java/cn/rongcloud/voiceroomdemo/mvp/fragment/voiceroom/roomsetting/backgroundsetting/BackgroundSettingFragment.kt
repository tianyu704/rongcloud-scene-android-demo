/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting

import androidx.recyclerview.widget.GridLayoutManager
import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.common.showToast
import cn.rongcloud.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import cn.rongcloud.voiceroomdemo.ui.widget.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_background_setting.*
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/22
 */
@AndroidEntryPoint
class BackgroundSettingFragment(
    view: IBackgroundSettingView
) :
    BaseBottomSheetDialogFragment<BackgroundSettingPresenter, IBackgroundSettingView>(R.layout.fragment_background_setting),
    IBackgroundSettingView by view {

    @Inject
    lateinit var roomInfoBean:VoiceRoomBean

    @Inject
    lateinit var roomModel: VoiceRoomModel

    @Inject
    lateinit var presenter:BackgroundSettingPresenter

    private var adapter: BackgroundSettingAdapter? = null
    override fun initPresenter(): BackgroundSettingPresenter {
        return presenter
    }

    override fun initView() {
        val itemDecoration = GridSpacingItemDecoration(
            (rv_background_list.layoutManager as GridLayoutManager).spanCount,
            resources.getDimensionPixelSize(R.dimen.background_setting_decoration), true
        )
        adapter = BackgroundSettingAdapter { selectBackground ->
            adapter?.selectBackground(selectBackground)
        }
        rv_background_list.addItemDecoration(itemDecoration)
        rv_background_list.adapter = adapter

        tv_confirm.setOnClickListener {

            adapter?.currentSelectedBackground?.let {
                roomModel
                    .setRoomBackground(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result ->
                        requireActivity().showToast(if (result) "设置成功" else "设置失败")
                        dismiss()
                    }
            }
        }
    }

    override fun onBackgroundList(backGroundUrlList: List<String>) {
        (rv_background_list.adapter as? BackgroundSettingAdapter)?.refreshData(
            backGroundUrlList,
            roomInfoBean.backgroundUrl
        )
    }

}