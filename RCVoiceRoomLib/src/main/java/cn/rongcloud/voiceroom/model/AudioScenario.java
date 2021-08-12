/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

/**
 * 音频模式
 */
public enum AudioScenario {
    /**
     * 默认场景。
     *
     * <p>
     */
    DEFAULT,

    /**
     * 音乐聊天室场景。此场景下，会使用音乐通道播放声音，音质好于通话通道。
     *
     * <p>
     */
    MUSIC_CHATROOM,

    /**
     * 音乐教室场景。此场景下，拾音范围会变广，同时不再处理噪音和回声。适用于乐器演奏等场景。注意，此场景下需要单工模式，通话模式下会有回声和噪音问题。
     *
     * <p>
     */
    MUSIC_CLASSROOM;

}
