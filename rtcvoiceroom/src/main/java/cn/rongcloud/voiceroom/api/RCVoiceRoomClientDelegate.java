/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import android.app.Application;

import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gusd
 * @Date 2021/07/02
 */
interface RCVoiceRoomClientDelegate {
    void initWithAppKey(Application context, String appKey);

    void connectWithToken(String token, ConnectCallback callback);

    void disconnect(boolean push);

    void registerMessageTypes(Class<? extends MessageContent>... classes);

    void setReceiveMessageDelegate(IRongCoreListener.OnReceiveMessageListener listener);

    void sendMessage(String targetId, MessageContent content, SendMessageCallback callback);


    interface ConnectCallback {

        void onSuccess(String userId);

        void onError(int code);

        void onDatabaseOpened(int code);
    }

    public interface SendMessageCallback {
        void onAttached(Message message);

        void onSuccess(Message message);

        void onError(Message message, int code, String reason);
    }

}
