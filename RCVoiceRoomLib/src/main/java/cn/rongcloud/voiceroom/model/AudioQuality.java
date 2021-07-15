/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

/**
 * @author gusd
 * @Date 2021/07/15
 */
/**
 * 设置音频通话质量, 默认为普通通话模式
 */
public enum AudioQuality {
    /**
     * 人声音质，编码码率最大值为32Kbps
     */
    SPEECH,
    /**
     * 标清音乐音质，编码码率最大值为64Kbps
     */
    MUSIC,
    /**
     * 高清音乐音质，编码码率最大值为128Kbps
     */
    MUSIC_HIGH;

}
