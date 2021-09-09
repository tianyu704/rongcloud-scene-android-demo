/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom2

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import cn.rongcloud.voiceroom.net.bean.respond.VoiceRoomBean
import cn.rongcloud.voiceroomdemo.R
import com.rongcloud.common.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_voice_room_scoll.*
import java.io.Serializable


private const val TAG = "ScorlVoiceRoomActivity"
private const val KEY_INDEX = "KEY_INDEX"
private const val KEY_ROOMS = "KEY_ROOMS"

@HiltBinding(value = IScrolVoiceRoomView::class)
@AndroidEntryPoint
class ScorlVoiceRoomActivity : BaseActivity() {


    class RoomInfo : Serializable {
        var roomId: String = ""
        var createrId: String = ""
        var isCreate: Boolean = false
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
                    if (currentRoomId == vr.roomId) {
                        index = i
                        this.isCreate = isCreate
                    }
                })
                i++
            }
            Intent(context, ScorlVoiceRoomActivity::class.java).apply {
                putExtra(KEY_INDEX, index)
                putExtra(KEY_ROOMS, list)
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
    }

    override fun initView() {
        scrol_vpager.orientation = ViewPager2.ORIENTATION_VERTICAL
        scrol_vpager.adapter = sAdapter
        scrol_vpager.setCurrentItem(pageIndex, false)
//        scrol_vpager.offscreenPageLimit = 3
    }

    override fun initData() {
        RCVoiceRoomEngine.getInstance().initEngine()
//        enableScroll(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: 2021/9/8 悬浮框需修改
        RCVoiceRoomEngine.getInstance().unInitEngine()
    }

    fun enableScroll(canScroll: Boolean) {
        // 是否禁止用户滑动页面
        scrol_vpager.isUserInputEnabled = canScroll
    }

    val sAdapter by lazy {
        return@lazy ScorlPageAdapter(this).apply {
            this.initData(list)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (sAdapter.getCurrentFragment().dispatchTouchEvent(ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    class ScorlPageAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        private var datas: List<RoomInfo> = ArrayList()
        fun initData(list: List<RoomInfo>?) {
            this.datas = list ?: arrayListOf()
            notifyDataSetChanged()
            Log.d(TAG, "initData list = " + datas.size)
        }

        private lateinit var current: VoiceRoomFragment
        fun getCurrentFragment(): VoiceRoomFragment {
            return current
        }

        override fun getItemCount(): Int = datas.size

        override fun createFragment(position: Int): Fragment {
            Log.d(TAG, "createFragment position = " + position)
            var room = datas.get(position)
            current = VoiceRoomFragment.newInstance(room.roomId, room.createrId, room.isCreate)
            return current
        }

    }
}












