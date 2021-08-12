/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.aroom;

import android.app.Application;

import java.util.Arrays;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public final class RCIMLibReceiver implements RCVoiceRoomClientDelegate {
    private static final String TAG = "RCIMLibReceiver";

    @Override
    public void initWithAppKey(Application context, String appKey) {
        RongCoreClient.init(context, appKey);
    }

    @Override
    public void connectWithToken(String token, final ConnectCallback callback) {
        RongCoreClient.connect(token, new IRongCoreCallback.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                callback.onSuccess(s);
            }

            @Override
            public void onError(IRongCoreEnum.ConnectionErrorCode connectionErrorCode) {
                callback.onError(connectionErrorCode.getValue());
            }

            @Override
            public void onDatabaseOpened(IRongCoreEnum.DatabaseOpenStatus databaseOpenStatus) {
                callback.onDatabaseOpened(databaseOpenStatus.getValue());
            }
        });
    }

    @Override
    public void disconnect(boolean push) {
        RongCoreClient.getInstance().disconnect(push);
    }

    @Override
    public void registerMessageTypes(Class<? extends MessageContent>... classes) {
        RongCoreClient.registerMessageType(Arrays.asList(classes));
    }

    @Override
    public void setReceiveMessageDelegate(IRongCoreListener.OnReceiveMessageListener listener) {
        RongCoreClient.setOnReceiveMessageListener(listener);
    }

    @Override
    public void sendMessage(String targetId, final MessageContent content, final SendMessageCallback callback) {
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM, targetId, content, "", "", new IRongCoreCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {
            }

            @Override
            public void onSuccess(Message message) {
                callback.onSuccess(message);
            }

            @Override
            public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                callback.onError(message, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }
}
