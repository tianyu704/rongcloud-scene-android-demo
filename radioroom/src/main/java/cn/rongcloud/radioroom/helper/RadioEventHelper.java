package cn.rongcloud.radioroom.helper;

import android.text.TextUtils;

import com.kit.UIKit;
import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.messager.SendMessageCallback;
import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.rroom.RCRadioEventListener;
import io.rong.imkit.picture.tools.ToastUtils;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/10/13
 * <p>
 * 维护一个监听的单例，方便房间最小化后状态的监听
 */
public class RadioEventHelper implements IRadioEventHelper, RCRadioEventListener {

    private List<RadioRoomListener> listeners = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private String roomId;

    public static RadioEventHelper getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void register(String roomId) {
        this.roomId = roomId;
        RCRadioRoomEngine.getInstance().setRadioEventListener(this);
    }

    @Override
    public void unRegister() {
        this.roomId = null;
        listeners.clear();
        messages.clear();
        Logger.e("==============RadioEventHelper:unRegister");
    }

    @Override
    public void addRadioEventListener(RadioRoomListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Logger.e("==============addRadioEventListener:messages-" + messages.size());
            if (!messages.isEmpty()) {
                listener.onLoadMessageHistory(messages);
            }
        }
    }

    @Override
    public void removeRadioEventListener(RadioRoomListener listener) {
        listeners.remove(listener);
        Logger.e("==============RadioEventHelper:removeRadioEventListener");
    }

    @Override
    public boolean isInRoom() {
        return !TextUtils.isEmpty(roomId);
    }

    @Override
    public void sendMessage(MessageContent messageContent) {
        if (TextUtils.isEmpty(roomId)) {
            Logger.e("roomId is empty, please register");
            return;
        }
        // 本地消息不发送出去
        if (messageContent instanceof RCChatroomLocationMessage) {
            Message message = new Message();
            message.setContent(messageContent);
            onMessageReceived(message);
            return;
        }
        RCMessager.getInstance().sendChatRoomMessage(roomId, messageContent, new SendMessageCallback() {
            @Override
            public void onAttached(Message message) {
            }

            @Override
            public void onSuccess(Message message) {
                onMessageReceived(message);
                Logger.e("=============sendChatRoomMessage:success");
            }

            @Override
            public void onError(Message message, int code, String reason) {
                ToastUtils.s(UIKit.getContext(), "发送失败");
                Logger.e("=============" + code + ":" + reason);
            }
        });
    }

    @Override
    public void onMessageReceived(Message message) {
        messages.add(message);
        for (RCRadioEventListener l : listeners) {
            l.onMessageReceived(message);
        }
    }

    @Override
    public void onAudienceEnter(String s) {
        for (RCRadioEventListener l : listeners) {
            l.onAudienceEnter(s);
        }
    }

    @Override
    public void onAudienceLeave(String s) {
        for (RCRadioEventListener l : listeners) {
            l.onAudienceLeave(s);
        }
    }

    @Override
    public void onRadioRoomKVUpdate(IRCRadioRoomEngine.UpdateKey updateKey, String s) {
        for (RCRadioEventListener l : listeners) {
            l.onRadioRoomKVUpdate(updateKey, s);
        }
    }

    private static class Holder {
        static final RadioEventHelper INSTANCE = new RadioEventHelper();
    }
}
