package cn.rongcloud.liveroom.helper;

import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.sdk.event.listener.LeaveRoomCallBack;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import io.rong.imlib.model.MessageContent;

/**
 * @author lihao
 * @project RongRTCDemo
 * @date 2021/11/16
 * @time 5:26 下午
 */
public interface ILiveEventHelper {

    /**
     * 注册
     *
     * @param roomId
     */
    void register(String roomId);

    /**
     * 反注册
     */
    void unRegister();

    /**
     * 发送消息
     *
     * @param messageContent
     */
    void sendMessage(MessageContent messageContent, boolean isShowLocation);

    /**
     * 获取当前用户的麦位状态
     */
    CurrentStatusType getCurrentStatus();

    /**
     * 保存当前请求上麦的状态
     */
    void setCurrentStatus(CurrentStatusType currentStatus);

    /**
     * 离开房间
     */
    void leaveRoom(LeaveRoomCallBack callback);

    /**
     * 邀请上麦
     */
    void pickUserToSeat(String userId, ClickCallback<Boolean> callback);

    /**
     * 同意上麦
     */
    void acceptRequestSeat(String userId, ClickCallback<Boolean> callback);

    /**
     * 撤销麦位申请
     */
    void cancelRequestSeat(ClickCallback<Boolean> callback);

    /**
     * 锁麦
     */
    void lockSeat(int index, boolean isClose, ClickCallback<Boolean> callback);

    /**
     * 开麦或者静麦
     *
     * @param index
     * @param isMute
     * @param callback
     */
    void muteSeat(int index, boolean isMute, ClickCallback<Boolean> callback);

    /**
     * 踢出房间
     */
    void kickUserFromRoom(User user, ClickCallback<Boolean> callback);

    /**
     * 抱下麦位
     *
     * @param user
     * @param callback
     */
    void kickUserFromSeat(User user, ClickCallback<Boolean> callback);

    /**
     * 更改所属于房间
     *
     * @param roomId
     */
    void changeUserRoom(String roomId);

}
