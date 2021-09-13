package cn.rongcloud.voiceroom.sdk;

import com.kit.wapper.IResultBack;

import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;

/**
 * 语聊房SDK api封装接口
 */
public interface Api {
    /**
     * notify room 的name
     */
    String EVENT_ROOM_CLOSE = "VoiceRoomClosed";
    String EVENT_BACKGROUND_CHANGE = "VoiceRoomBackgroundChanged";
    String EVENT_MANAGER_LIST_CHANGE = "VoiceRoomNeedRefreshManagerList";
    String EVENT_REJECT_MANAGE_PICK = "VoiceRoomRejectManagePick"; // 拒绝上麦
    String EVENT_AGREE_MANAGE_PICK = "VoiceRoomAgreeManagePick"; // 同意上麦
    String EVENT_KICK_OUT_OF_SEAT = "EVENT_KICK_OUT_OF_SEAT";
    String EVENT_REQUEST_SEAT_REFUSE = "EVENT_REQUEST_SEAT_REFUSE";
    String EVENT_REQUEST_SEAT_AGREE = "EVENT_REQUEST_SEAT_AGREE";
    String EVENT_REQUEST_SEAT_CANCEL = "EVENT_REQUEST_SEAT_CANCEL";
    String EVENT_USER_LEFT_SEAT = "EVENT_USER_LEFT_SEAT";


    String EVENT_KICKED_OUT_OF_ROOM = "EVENT_KICKED_OUT_OF_ROOM";

    RCVoiceRoomInfo getRoomInfo();

    void notifyRoom(String name, String content);

    void createAndJoin(String roomId, RCVoiceRoomInfo roomInfo, IResultBack<Boolean> resultBack);

    void joinRoom(String roomId, IResultBack<Boolean> resultBack);

    void leaveRoom(IResultBack<Boolean> resultBack);

    /**
     * 全麦锁定
     */
    void lockAll(boolean locked);

    void lockSeat(int index, boolean locked, IResultBack<Boolean> resultBack);

    /**
     * 全麦静音
     */
    void muteAll(boolean mute);


    void muteSeat(int index, boolean mute, IResultBack<Boolean> resultBack);

    void leaveSeat(IResultBack<Boolean> resultBack);

    void enterSeat(int index, IResultBack<Boolean> resultBack);

    void requestSeat(IResultBack<Boolean> resultBack);

    void cancelRequestSeat(IResultBack<Boolean> resultBack);

    void acceptRequestSeat(String userId, IResultBack<Boolean> resultBack);

    void rejectRequestSeat(String userId, IResultBack<Boolean> resultBack);

    void updateSeatExtra(int seatIndex, String extra, IResultBack<Boolean> resultBack);

    /**
     * 跟新麦位count
     *
     * @param count
     * @param resultBack
     */
    void updateSeatCount(int count, IResultBack<Boolean> resultBack);

    /**
     * 跟新房间名称
     *
     * @param name
     * @param resultBack
     */
    void updateRoomName(String name, IResultBack<Boolean> resultBack);

}
