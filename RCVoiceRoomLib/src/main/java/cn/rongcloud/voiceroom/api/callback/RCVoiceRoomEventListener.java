/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api.callback;

import java.util.List;

import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import io.rong.imlib.model.Message;


/**
 * @author gusd
 * @Date 2021/05/31
 */
public interface RCVoiceRoomEventListener {
    /**
     * 房间准备就绪
     */
    public void onRoomKVReady();

    // TODO: 2021/6/29 房间异常回调

    /**
     * 房间信息变更回调
     *
     * @param roomInfo 房间信息 {@link RCVoiceRoomInfo}
     */
    public void onRoomInfoUpdate(RCVoiceRoomInfo roomInfo);

    /**
     * 房间座位变更回调，包括自身上麦或下麦也会触发此回调
     *
     * @param seatInfoList 座位列表信息 {@link RCVoiceRoomInfo}
     */
    public void onSeatInfoUpdate(List<RCVoiceSeatInfo> seatInfoList);

    /**
     * 某个主播上麦回调，包含自己上麦也会触发此回调
     *
     * @param seatIndex 麦位号
     * @param userId    用户Id
     */
    public void onUserEnterSeat(int seatIndex, String userId);

    /**
     * 某个主播下麦回调，包含自己下麦也会触发此回调
     *
     * @param seatIndex 麦位号
     * @param userId    用户Id
     */
    public void onUserLeaveSeat(int seatIndex, String userId);

    /**
     * 座位静音状态回调
     *
     * @param index  座位号
     * @param isMute 静音状态
     */
    public void onSeatMute(int index, boolean isMute);

    /**
     * 座位关闭回调
     *
     * @param index  座位号
     * @param isLock 是否关闭
     */
    public void onSeatLock(int index, boolean isLock);

    /**
     * 观众进房回调
     *
     * @param userId 观众 Id
     */
    public void onAudienceEnter(String userId);

    /**
     * 观众退房回调
     *
     * @param userId 观众 Id
     */
    public void onAudienceExit(String userId);

    /**
     * 用户音量变动回调
     *
     * @param seatIndex  麦位序号
     * @param isSpeaking 是否正在说话
     */
    public void onSpeakingStateChanged(int seatIndex, boolean isSpeaking);

    /**
     * 收取消息回调
     *
     * @param message 收到的消息
     */
    public void onMessageReceived(Message message);

    /**
     * 房间通知回调
     *
     * @param name    名称
     * @param content 内容
     */
    public void onRoomNotificationReceived(String name, String content);

    /**
     * 自己被抱上麦通知
     */
    public void onPickSeatReceivedFrom(String userId);

    /**
     * 自己被下麦通知
     */
    public void onKickSeatReceived(int index);

    /**
     * 发送的排麦请求得到房主或管理员同意
     */
    public void onRequestSeatAccepted();

    /**
     * 发送的排麦请求被房主或管理员拒绝
     */
    public void onRequestSeatRejected();

    /**
     * 排麦列表发生变化
     */
    public void onRequestSeatListChanged();

    /**
     * 收到邀请回调
     *
     * @param invitationId 邀请的 Id
     * @param userId       发送邀请的用户
     * @param content      邀请内容，用户可以自定义
     */
    public void onInvitationReceived(String invitationId, String userId, String content);

    /**
     * 邀请被接受回调
     *
     * @param invitationId 邀请 Id
     */
    public void onInvitationAccepted(String invitationId);

    /**
     * 邀请被拒绝回调
     *
     * @param invitationId 邀请 Id
     */
    public void onInvitationRejected(String invitationId);

    /**
     * 邀请被取消回调
     *
     * @param invitationId 邀请 Id
     */
    public void onInvitationCancelled(String invitationId);

    /**
     * 被踢出房间回调
     *
     * @param targetId 被踢人 Id
     * @param userId   发起人 Id
     */
    public void onUserReceiveKickOutRoom(String targetId, String userId);

}
