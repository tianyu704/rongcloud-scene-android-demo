/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 语聊房错误枚举
 */
public enum VoiceRoomErrorCode {
    Unknown_Error(-1, "Unknown error"),
    /**
     * 操作成功
     */
    RCVoiceRoomSuccess(70000, "Success"),
    /**
     * 连接服务器失败
     */
    RCVoiceRoomConnectTokenFailed(70001, "Init token failed"),
    /**
     * 麦位序号不对
     */
    RCVoiceRoomSeatIndexOutOfRange(70002, "Seat index not correct"),
    /**
     * 用户已经在麦位上
     */
    RCVoiceRoomUserAlreadyOnSeat(70003, "User is on seat now"),
    /**
     * 用户不在麦位上
     */
    RCVoiceRoomUserNotOnSeat(70004, "User Not on seat now"),
    /**
     * 用户跳麦的麦位和之前的一样
     */
    RCVoiceRoomJumpIndexEqual(70005, "Target index can't equal to current index"),
    /**
     * 麦位不是空置状态
     */
    RCVoiceRoomSeatNotEmpty(70006, "Seat is locked or using"),
    /**
     * 不能抱自己上麦
     */
    RCVoiceRoomPickSelfToSeat(70007, "User can't pick self on seat"),
    /**
     * 发送抱麦请求失败
     */
    RCVoiceRoomPickUserFailed(70008, "Pick user seat failed"),
    /**
     * 不能踢自己下麦
     */
    RCVoiceRoomUserKickSelfFromSeat(70009, "User can't kick self"),
    /**
     * 加入语聊房失败
     */
    RCVoiceRoomJoinRoomFailed(70010, "Join ChatRoom Failed"),
    /**
     * 离开语聊房失败
     */
    RCVoiceRoomLeaveRoomFailed(70011, "Leave chat or rtc room failed"),
    /**
     * 获取房间信息失败
     */
    RCVoiceRoomGetRoomInfoFailed(70012, "获取房间信息失败"),
    /**
     * 已经在排麦列表中了
     */
    RCVoiceRoomAlreadyInRequestList(70013, "User already on seat"),
    /**
     * 排麦人数太多
     */
    RCVoiceRoomRequestListFull(70014, "Request seat list is full, the max is 20"),
    /**
     * 排麦请求发送失败
     */
    RCVoiceRoomSendRequestSeatFailed(70015, "Send request seat failed"),
    /**
     * 取消排麦请求发送失败
     */
    RCVoiceRoomCancelRequestSeatFailed(70016, "Cancel request seat failed"),
    /**
     * 同意排麦请求发送失败
     */
    RCVoiceRoomAcceptRequestSeatFailed(70017, "Accept request seat failed"),
    /**
     * 拒绝排麦请求发送失败
     */
    RCVoiceRoomRejectRequestSeatFailed(70018, "拒绝排麦请求发送失败"),
    /**
     * 同步麦位信息失败
     */
    RCVoiceRoomSyncSeatInfoFailed(70019, "sync seat info failed"),
    /**
     * 同步房间信息失败
     */
    RCVoiceRoomSyncRoomInfoFailed(70020, "setup Room Info failed"),
    /**
     * 同步排麦相关信息
     */
    RCVoiceRoomSyncRequestSeatFailed(70021, "update waiting kv failed"),
    /**
     * 获取排麦请求列表失败
     */
    RCVoiceRoomGetRequestListFailed(70022, "Get entries failed"),

    /**
     * 发送信息失败
     */
    RCVoiceRoomSendMessageFailed(70023, "Send message failed"),
    /**
     * 请求发送失败
     */
    RCVoiceRoomSendInvitationSeatFailed(70024, "send invitation failed"),
    /**
     * 取消请求发送失败
     */
    RCVoiceRoomCancelInvitationFailed(70025, "cancel invitation failed"),
    /**
     * 同意请求发送失败
     */
    RCVoiceRoomAcceptInvitationFailed(70026, "accept invitation failed"),
    /**
     * 拒绝请求发送失败
     */
    RCVoiceRoomRejectInvitationFailed(70027, "reject invitation failed"),
    /**
     * 创建语聊房失败
     */
    RCVoiceRoomCreateRoomFailed(70028, "Create room failed"),
    /**
     * 用户ID为空
     */
    RCVoiceRoomUserIdIsEmpty(70029, "Room or User Id is Empty");


    // 使用 map 提高查询效率
    private static final Map<Integer, VoiceRoomErrorCode> map = new HashMap<>();

    static {
        for (VoiceRoomErrorCode value : VoiceRoomErrorCode.values()) {
            map.put(value.code, value);
        }
    }

    private final int code;
    private final String message;

    VoiceRoomErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static VoiceRoomErrorCode valueOf(int code) {
        VoiceRoomErrorCode errorCode = map.get(code);
        return errorCode == null ? Unknown_Error : errorCode;
    }

    /**
     * 获取错误码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误描述信息
     *
     * @return 描述信息
     */
    public String getMessage() {
        return message;
    }
}
