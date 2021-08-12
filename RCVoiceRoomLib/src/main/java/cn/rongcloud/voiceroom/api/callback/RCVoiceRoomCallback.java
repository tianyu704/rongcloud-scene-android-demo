/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api.callback;

/**
 * 无返回结果的回调
 */
public interface RCVoiceRoomCallback extends RCVoiceRoomBaseCallback {

    /**
     * 成功会滴
     */
    void onSuccess();

    @Override
    void onError(int code, String message);
}
