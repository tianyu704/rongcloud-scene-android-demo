/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import android.app.Application;

import java.util.Arrays;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gusd
 * @Date 2021/07/02
 */
final class RCIMKitReceiver implements RCVoiceRoomClientDelegate {
    private static final String TAG = "RCIMKitReceiver";

    public void initWithAppKey(Application context, String appKey) {
        RongIM.init(context, appKey);

    }

    @Override
    public void connectWithToken(String token, final ConnectCallback callback) {
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                callback.onSuccess(s);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode connectionErrorCode) {
                callback.onError(connectionErrorCode.getValue());
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                callback.onDatabaseOpened(databaseOpenStatus.getValue());
            }
        });
    }

    @Override
    public void disconnect(boolean push) {
        RongIM.getInstance().disconnect();
    }

    @Override
    public void registerMessageTypes(Class<? extends MessageContent>... classes) {
        RongIMClient.registerMessageType(Arrays.asList(classes));
    }

    @Override
    public void setReceiveMessageDelegate(final IRongCoreListener.OnReceiveMessageListener listener) {
        RongIM.addOnReceiveMessageListener(new RongIMClient.OnReceiveMessageWrapperListener() {
            @Override
            public boolean onReceived(Message message, int i, boolean b, boolean b1) {
                listener.onReceived(message, i);
                return false;
            }
        });
    }

    @Override
    public void sendMessage(String targetId, MessageContent content, final SendMessageCallback callback) {
        RongIM
                .getInstance()
                .sendMessage(Conversation.ConversationType.CHATROOM
                        , targetId
                        , content
                        , ""
                        , ""
                        , new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(Message message) {
                                callback.onSuccess(message);
                            }

                            @Override
                            public void onSuccess(Message message) {
                                callback.onSuccess(message);
                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                                callback.onError(message, errorCode.getValue(), errorCode.getMessage());
                            }
                        });
    }
}
