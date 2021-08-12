/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api.callback;


public interface RCVoiceRoomBaseCallback {
    /**
     * 错误回调
     *
     * @param code    错误吗
     * @param message 错误的描述信息
     */
    void onError(int code, String message);
}
