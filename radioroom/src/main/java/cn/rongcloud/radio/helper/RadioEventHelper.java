package cn.rongcloud.radio.helper;

import android.text.TextUtils;
import android.util.Log;

import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.kit.UIKit;
import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.common.utils.JsonUtils;
import cn.rong.combusis.manager.AllBroadcastManager;
import cn.rong.combusis.message.RCAllBroadcastMessage;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.message.RCFollowMsg;
import cn.rong.combusis.music.MusicManager;
import cn.rong.combusis.widget.miniroom.OnCloseMiniRoomListener;
import cn.rong.combusis.widget.miniroom.OnMiniRoomListener;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.messager.SendMessageCallback;
import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.callback.RCRadioRoomCallback;
import cn.rongcloud.radioroom.room.RCRadioEventListener;
import io.rong.imkit.picture.tools.ToastUtils;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/10/13
 * <p>
 * 维护一个监听的单例，方便房间最小化后状态的监听
 */
public class RadioEventHelper implements IRadioEventHelper, RCRadioEventListener, OnCloseMiniRoomListener {

    private List<RadioRoomListener> listeners = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private OnMiniRoomListener onMiniRoomListener;
    private String roomId;
    // 是否在麦位上
    private boolean isInSeat = false;
    // 是否暂停
    private boolean isSuspend = false;
    // 是否静音
    private boolean isMute = false;

    public static RadioEventHelper getInstance() {
        return Holder.INSTANCE;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isInSeat() {
        return isInSeat;
    }

    public void setInSeat(boolean inSeat) {
        isInSeat = inSeat;
    }

    public boolean isSuspend() {
        return isSuspend;
    }

    public void setSuspend(boolean suspend) {
        isSuspend = suspend;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    @Override
    public void register(String roomId) {
        if (!TextUtils.equals(roomId, this.roomId)) {
            this.roomId = roomId;
            RCRadioRoomEngine.getInstance().setRadioEventListener(this);
        }
    }

    @Override
    public void unRegister() {
        this.roomId = null;
        listeners.clear();
        messages.clear();
        isInSeat = false;
        isSuspend = false;
        isMute = false;
        onMiniRoomListener = null;
    }

    @Override
    public void addRadioEventListener(RadioRoomListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Logger.e("==============addRadioEventListener:messages-" + messages.size() + " listener size:" + listeners.size());
            if (!messages.isEmpty()) {
                listener.onLoadMessageHistory(messages);
            }
        }
    }

    public void setMiniRoomListener(OnMiniRoomListener onMiniRoomListener) {
        this.onMiniRoomListener = onMiniRoomListener;
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
        Logger.e("========================" + message.getContent() + JsonUtils.toJson(message.getContent()));
        if (message.getContent() instanceof RCAllBroadcastMessage) {
            AllBroadcastManager.getInstance().addMessage((RCAllBroadcastMessage) message.getContent());
            return;
        }
        if (isShowingMessage(message)) {
            messages.add(message);
        }
        for (RCRadioEventListener l : listeners) {
            l.onMessageReceived(message);
        }
    }

    public boolean isShowingMessage(Message message) {
        MessageContent content = message.getContent();
        if (content instanceof RCChatroomBarrage || content instanceof RCChatroomEnter
                || content instanceof RCChatroomKickOut || content instanceof RCChatroomGiftAll
                || content instanceof RCChatroomGift || content instanceof RCChatroomAdmin
                || content instanceof RCChatroomLocationMessage || content instanceof RCFollowMsg) {
            return true;
        }
        return false;
    }

    @Override
    public void onRadioRoomKVUpdate(IRCRadioRoomEngine.UpdateKey updateKey, String s) {
        for (RCRadioEventListener l : listeners) {
            l.onRadioRoomKVUpdate(updateKey, s);
        }
        if (onMiniRoomListener != null) {
            if (updateKey == IRCRadioRoomEngine.UpdateKey.RC_SPEAKING) {
                onMiniRoomListener.onSpeak(TextUtils.equals(s, "1"));
            }
        }
    }

    @Override
    public void onCloseMiniRoom(CloseResult closeResult) {
        onMiniRoomListener = null;
        // need leave room
        RCRadioRoomEngine.getInstance().leaveRoom(new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                changeUserRoom("");
                MusicManager.get().stopPlayMusic();
                unRegister();
                if (closeResult != null) {
                    closeResult.onClose();
                }
            }

            @Override
            public void onError(int code, String message) {
                MusicManager.get().stopPlayMusic();
                unRegister();
                if (closeResult != null) {
                    closeResult.onClose();
                }
            }
        });
    }

    //更改所属房间
    private void changeUserRoom(String roomId) {
        HashMap<String, Object> params = new OkParams()
                .add("roomId", roomId)
                .build();
        OkApi.get(VRApi.USER_ROOM_CHANGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    Log.e("TAG", "changeUserRoom: " + result.getBody());
                }
            }
        });
    }

    private static class Holder {
        static final RadioEventHelper INSTANCE = new RadioEventHelper();
    }
}
