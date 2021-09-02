/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom2

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.voiceroom.net.bean.respond.VoiceRoomBean
import cn.rongcloud.voiceroomdemo.R
import com.rongcloud.common.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_voice_room_scoll.*
import java.io.Serializable


private const val TAG = "ScorlVoiceRoomActivity"
private const val KEY_INDEX = "KEY_INDEX"
private const val KEY_ROOMS = "KEY_ROOMS"
private const val KEY_IS_CREATE = "KEY_IS_CREATE"

@HiltBinding(value = IScrolVoiceRoomView::class)
@AndroidEntryPoint
class ScorlVoiceRoomActivity : BaseActivity() {


    class RoomInfo : Serializable {
        var roomId: String = ""
        var createrId: String = ""
    }

    companion object {
        fun startActivity(
            context: Context,
            currentRoomId: String,
            voicerooms: List<VoiceRoomBean>,
            isCreate: Boolean = false
        ) {
            var list = ArrayList<RoomInfo>()
            var index = 0
            var i = 0
            voicerooms?.forEach { vr ->
                list.add(RoomInfo().apply {
                    this.roomId = vr.roomId
                    this.createrId = vr.createUser?.userId ?: ""
                })
                if (currentRoomId == vr.roomId) {
                    index = i
                }
                i++
            }
            Intent(context, ScorlVoiceRoomActivity::class.java).apply {
                putExtra(KEY_INDEX, index)
                putExtra(KEY_ROOMS, list)
                putExtra(KEY_IS_CREATE, isCreate)
                context.startActivity(this)
            }
        }
    }

    override fun isLightThemeActivity(): Boolean {
        return false
    }

    override fun getContentView(): Int {
        return R.layout.activity_voice_room_scoll
    }

    var pageIndex = 0
    var list: List<RoomInfo>? = null
    override fun beforeInitView() {
        pageIndex = intent.getIntExtra(KEY_INDEX, 0)
        list = intent.getSerializableExtra(KEY_ROOMS) as List<RoomInfo>
        Log.d(TAG, "beforeInitView pageIndex = " + pageIndex)
        Log.d(TAG, "beforeInitView list = " + (list?.size ?: 0))
    }

    override fun initView() {
        scrol_vpager.orientation = ViewPager2.ORIENTATION_VERTICAL
        scrol_vpager.adapter = ScorlPageAdapter(this).apply {
            this.initData(list)
        }
        scrol_vpager.setCurrentItem(pageIndex, false)
    }

    override fun initData() {
    }

    fun enableScroll(canScroll: Boolean) {
        // 是否禁止用户滑动页面
        scrol_vpager.isUserInputEnabled = canScroll
    }

    class ScorlPageAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        private var datas: List<RoomInfo> = ArrayList()
        fun initData(list: List<RoomInfo>?) {
            this.datas = list ?: arrayListOf()
            notifyDataSetChanged()
            Log.d(TAG, "initData list = " + datas.size)
        }

        override fun getItemCount(): Int = datas.size

        override fun createFragment(position: Int): Fragment {
            Log.d(TAG, "createFragment position = " + position)
            var room = datas.get(position)
            return VoiceRoomFragment.newInstance(room.roomId, room.createrId)
        }

    }
}











