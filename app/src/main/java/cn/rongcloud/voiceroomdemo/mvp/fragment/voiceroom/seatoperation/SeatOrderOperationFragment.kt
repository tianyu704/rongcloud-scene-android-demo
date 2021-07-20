/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import cn.rongcloud.voiceroomdemo.mvp.fragment.BaseFragment
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import cn.rongcloud.voiceroomdemo.ui.uimodel.UiMemberModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_viewpage_list.*
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/24
 */
@AndroidEntryPoint
class SeatOrderOperationFragment(
    view: IViewPageListView,
    private val selectPageIndex: Int = 0
) :
    BaseBottomSheetDialogFragment<SeatOrderOperationPresenter, IViewPageListView>(R.layout.fragment_viewpage_list),
    IViewPageListView by view, IInviteSeatListView, IRequestSeatListView {

    @Inject
    lateinit var presenter: SeatOrderOperationPresenter

    override fun initPresenter(): SeatOrderOperationPresenter {
        return presenter
    }

    override fun initView() {
        val fragmentList =
            arrayListOf(RequestSeatListFragment(this), InviteSeatListFragment(this))

        vp_page.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vp_page.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }
        }

        TabLayoutMediator(
            tl_title, vp_page, true
        ) { tab, position ->
            tab.text = (fragmentList[position] as BaseFragment<*, *>).getTitle()
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        vp_page.setCurrentItem(selectPageIndex, true)
    }

    override fun refreshData(data: List<UiMemberModel>) {

    }


}