/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import android.util.Log
import android.widget.CompoundButton
import android.widget.SeekBar
import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.rtc.api.RCRTCAudioMixer
import cn.rongcloud.rtc.api.RCRTCEngine
import cn.rongcloud.voiceroomdemo.R
import com.kit.cache.SharedPreferUtil
import com.rongcloud.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_music_control.*
import javax.inject.Inject

// 耳返sp缓存key
val EAR_MONITORING = "key_earMonitoring_"

/**
 * @author gusd
 * @Date 2021/07/06
 */
@HiltBinding(value = IMusicControlView::class)
@AndroidEntryPoint
class MusicControlFragment(
    private val roomId: String,
    view: IMusicControlView
) :
    BaseFragment(
        R.layout.fragment_music_control
    ), IMusicControlView by view, SeekBar.OnSeekBarChangeListener {

    @Inject
    lateinit var presenter: MusicControlPresenter

    override fun initView() {
        sb_local_audio_setting.setOnSeekBarChangeListener(this)
        sb_mic_audio_setting.setOnSeekBarChangeListener(this)
        sb_remote_audio_setting.setOnSeekBarChangeListener(this)
        tv_local_audio_value.text = "${RCRTCAudioMixer.getInstance().playbackVolume}"
        sb_local_audio_setting.progress = RCRTCAudioMixer.getInstance().playbackVolume

        tv_mic_audio_value.text = "${RCRTCEngine.getInstance().defaultAudioStream.recordingVolume}"
        sb_mic_audio_setting.progress = RCRTCEngine.getInstance().defaultAudioStream.recordingVolume

        tv_remote_audio_value.text =
            "${RCRTCAudioMixer.getInstance().mixingVolume}"
        sb_remote_audio_setting.progress = RCRTCAudioMixer.getInstance().mixingVolume
        // 获取缓存的
        var enable = SharedPreferUtil.getBoolean(EAR_MONITORING + roomId)
        setEarMonitoringAndCache(enable)
        sw_checked.isChecked = enable
        sw_checked.setOnCheckedChangeListener { compoundButton: CompoundButton, checked: Boolean ->
            setEarMonitoringAndCache(checked)
        }
    }

    /**
     * 设置耳返
     */
    fun setEarMonitoringAndCache(enable: Boolean) {
        Log.e("setEarMonitoring", "enable = " + enable)
        RCRTCEngine.getInstance().defaultAudioStream.enableEarMonitoring(enable)//耳返
        SharedPreferUtil.set(EAR_MONITORING + roomId, enable)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar) {
            sb_local_audio_setting -> {
                tv_local_audio_value.text = "$progress"
                RCRTCAudioMixer.getInstance().playbackVolume = progress
            }
            sb_mic_audio_setting -> {
                tv_mic_audio_value.text = "$progress"
                RCRTCEngine.getInstance().defaultAudioStream.adjustRecordingVolume(progress)//麦克风音量
            }
            sb_remote_audio_setting -> {
                tv_remote_audio_value.text = "$progress"
                RCRTCAudioMixer.getInstance().mixingVolume = progress
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }


}