/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api.callback;

/**
 * @author gusd
 * @Date 2021/06/02
 */
public interface RCVoiceRoomResultCallback<T> extends RCVoiceRoomBaseCallback {
    void onSuccess(T data);
}