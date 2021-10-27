package cn.rongcloud.radioroom.room;

import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import io.rong.imlib.model.Message;

public interface RCRadioEventListener {

    /**
     * 收取消息回调
     *
     * @param message 收到的消息
     */
    void onMessageReceived(Message message);

    /**
     * 观众进入房间
     *
     * @param userId
     */
    void onAudienceEnter(String userId);

    /**
     * 观众离开房间
     *
     * @param userId
     */
    void onAudienceLeave(String userId);

    /**
     * 房间KV更新回调
     *
     * @param key
     * @param value
     */
    void onRadioRoomKVUpdate(IRCRadioRoomEngine.UpdateKey key, String value);
}
