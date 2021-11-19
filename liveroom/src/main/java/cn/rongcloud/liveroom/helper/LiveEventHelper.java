package cn.rongcloud.liveroom.helper;

import static cn.rong.combusis.provider.voiceroom.CurrentStatusType.STATUS_NOT_ON_SEAT;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.sdk.event.listener.LeaveRoomCallBack;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.MessageContent;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * @author lihao
 * @project RongRTCDemo
 * @date 2021/11/16
 * @time 5:30 下午
 * 用来直播房的各种监听事件  发送消息 麦位操作等
 * 维护一定的集合来返回事件
 */
public class LiveEventHelper implements ILiveEventHelper {

    private List<MessageContent> messageList;
    private String roomId;//直播房的房间ID
    private CurrentStatusType currentStatus = STATUS_NOT_ON_SEAT;

    public static LiveEventHelper getInstance() {
        return helper.INSTANCE;
    }

    @Override
    public CurrentStatusType getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public void setCurrentStatus(CurrentStatusType currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public void leaveRoom(LeaveRoomCallBack callback) {

    }

    @Override
    public void pickUserToSeat(String userId, ClickCallback<Boolean> callback) {

    }

    @Override
    public void acceptRequestSeat(String userId, ClickCallback<Boolean> callback) {

    }

    @Override
    public void cancelRequestSeat(ClickCallback<Boolean> callback) {

    }

    @Override
    public void lockSeat(int index, boolean isClose, ClickCallback<Boolean> callback) {

    }

    @Override
    public void muteSeat(int index, boolean isMute, ClickCallback<Boolean> callback) {

    }

    @Override
    public void kickUserFromRoom(User user, ClickCallback<Boolean> callback) {

    }

    @Override
    public void kickUserFromSeat(User user, ClickCallback<Boolean> callback) {

    }

    @Override
    public void changeUserRoom(String roomId) {

    }

    public List<MessageContent> getMessageList() {
        return messageList;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public void register(String roomId) {
        this.roomId = roomId;
        this.messageList = new ArrayList<>();
    }

    @Override
    public void unRegister() {
        this.roomId = null;
        messageList.clear();
    }

    /**
     * 发送消息
     *
     * @param messageContent 消息体
     * @param isShowLocation 是否在本地显示
     */
    @Override
    public void sendMessage(MessageContent messageContent, boolean isShowLocation) {
        if (!TextUtils.isEmpty(roomId))
            if (messageContent instanceof RCChatroomLocationMessage) {
                RCChatRoomMessageManager.INSTANCE.sendLocationMessage(roomId, messageContent);
            } else {
                RCChatRoomMessageManager.INSTANCE.sendChatMessage(roomId, messageContent, isShowLocation
                        , new Function1<Integer, Unit>() {
                            @Override
                            public Unit invoke(Integer integer) {
                                if (isShowLocation) {
                                    messageList.add(messageContent);
                                }
                                return null;
                            }
                        }, new Function2<IRongCoreEnum.CoreErrorCode, Integer, Unit>() {
                            @Override
                            public Unit invoke(IRongCoreEnum.CoreErrorCode coreErrorCode, Integer integer) {
                                EToast.showToast("发送失败");
                                return null;
                            }
                        });
            }

    }

    private static class helper {
        static final LiveEventHelper INSTANCE = new LiveEventHelper();
    }


}
