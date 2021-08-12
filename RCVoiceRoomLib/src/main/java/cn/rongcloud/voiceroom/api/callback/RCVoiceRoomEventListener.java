/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api.callback;

import java.util.List;

import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import io.rong.imlib.model.Message;

/**
 * 语聊房事件监听
 */
public interface RCVoiceRoomEventListener {
    /**
     * 房间KV准备就绪
     */
    void onRoomKVReady();

    /**
     * 房间信息变更
     *
     * @param roomInfo 房间信息 {@link RCVoiceRoomInfo}
     */
    void onRoomInfoUpdate(RCVoiceRoomInfo roomInfo);

    /**
     * 房间座位变更，注意：自身上麦或下麦也会触发此回调
     *
     * @param seatInfoList 座位列表信息 {@link RCVoiceRoomInfo}
     */
    void onSeatInfoUpdate(List<RCVoiceSeatInfo> seatInfoList);

    /**
     * 主播上麦，注意：自己上麦也会触发此回调
     *
     * @param seatIndex 麦位号
     * @param userId    用户Id
     */
    void onUserEnterSeat(int seatIndex, String userId);

    /**
     * 主播下麦，注意：自己下麦也会触发此回调
     *
     * @param seatIndex 麦位号
     * @param userId    用户Id
     */
    void onUserLeaveSeat(int seatIndex, String userId);

    /**
     * 座位静音状态回调
     *
     * @param index  座位号
     * @param isMute 静音状态
     */
    void onSeatMute(int index, boolean isMute);

    /**
     * 座位锁定状态回调
     *
     * @param index  座位号
     * @param isLock 是否关闭
     */
    void onSeatLock(int index, boolean isLock);

    /**
     * 观众进房回调
     *
     * @param userId 观众 Id
     */
    void onAudienceEnter(String userId);

    /**
     * 观众退房回调
     *
     * @param userId 观众 Id
     */
    void onAudienceExit(String userId);

    /**
     * 用户音量变动回调
     *
     * @param seatIndex  麦位序号
     * @param isSpeaking 是否正在说话
     */
    void onSpeakingStateChanged(int seatIndex, boolean isSpeaking);

    /**
     * 收取消息回调
     *
     * @param message 收到的消息
     */
    void onMessageReceived(Message message);

    /**
     * 房间通知回调
     *
     * @param name    名称
     * @param content 通知内容
     */
    void onRoomNotificationReceived(String name, String content);

    /**
     * 用户被抱上麦
     */
    void onPickSeatReceivedFrom(String userId);

    /**
     * 自主播被下麦
     */
    void onKickSeatReceived(int index);

    /**
     * 房主或管理员 接受同意 用户的排麦申请
     */
    void onRequestSeatAccepted();

    /**
     * 发送的排麦请求被房主或管理员拒绝
     */
    void onRequestSeatRejected();

    /**
     * 排麦列表发生变化
     */
    void onRequestSeatListChanged();

    /**
     * 收到上麦邀请
     *
     * @param invitationId 邀请标识 Id
     * @param userId       发送邀请用户的标识
     * @param content      邀请内容 （用户可以自定义）
     */
    void onInvitationReceived(String invitationId, String userId, String content);

    /**
     * 邀请被接受通知
     *
     * @param invitationId 邀请标识 Id
     */
    void onInvitationAccepted(String invitationId);

    /**
     * 邀请被拒绝回调
     *
     * @param invitationId 邀请标识 Id
     */
    void onInvitationRejected(String invitationId);

    /**
     * 邀请被取消回调
     *
     * @param invitationId 邀请标识 Id
     */
    void onInvitationCancelled(String invitationId);

    /**
     * 被踢出房间回调
     *
     * @param targetId 被踢用户的标识
     * @param userId   发起踢人用户的标识
     */
    void onUserReceiveKickOutRoom(String targetId, String userId);

}
