/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gusd
 * @Date 2021/06/02
 */
public enum VoiceRoomErrorCode {
    
    /// 操作成功
    RCVoiceRoomSuccess(70000,"操作成功"),
    /// 连接服务器失败
    RCVoiceRoomConnectTokenFailed(70000,"连接服务器失败"),
    /// 麦位序号不对
    RCVoiceRoomSeatIndexOutOfRange(70000,"麦位序号不对"),
    /// 用户已经在麦位上
    RCVoiceRoomUserAlreadyOnSeat(70000,"用户已经在麦位上"),
    /// 用户不在麦位上
    RCVoiceRoomUserNotOnSeat(70000,"用户不在麦位上"),
    /// 用户跳麦的麦位和之前的一样
    RCVoiceRoomJumpIndexEqual(70000,"用户跳麦的麦位和之前的一样"),
    /// 麦位不是空置状态
    RCVoiceRoomSeatNotEmpty(70000,"麦位不是空置状态"),
    /// 不能抱自己上麦
    RCVoiceRoomPickSelfToSeat(70000,"不能抱自己上麦"),
    /// 发送抱麦请求失败
    RCVoiceRoomPickUserFailed(70000,"发送抱麦请求失败"),
    /// 不能踢自己下麦
    RCVoiceRoomUserKickSelfFromSeat(70000,"不能踢自己下麦"),
    /// 加入语聊房失败
    RCVoiceRoomJoinRoomFailed(70000,"加入语聊房失败"),
    /// 离开语聊房失败
    RCVoiceRoomLeaveRoomFailed(70000,"离开语聊房失败"),
    /// 获取房间信息失败
    RCVoiceRoomGetRoomInfoFailed(70000,"获取房间信息失败"),
    /// 已经在排麦列表中了
    RCVoiceRoomAlreadyInRequestList(70000,"已经在排麦列表中了"),
    /// 排麦人数太多
    RCVoiceRoomRequestListFull(70000,"排麦人数太多"),
    /// 排麦请求发送失败
    RCVoiceRoomSendRequestSeatFailed(70000,"排麦请求发送失败"),
    /// 取消排麦请求发送失败
    RCVoiceRoomCancelRequestSeatFailed(70000,"取消排麦请求发送失败"),
    /// 同意排麦请求发送失败
    RCVoiceRoomAcceptRequestSeatFailed(70000,"同意排麦请求发送失败"),
    /// 拒绝排麦请求发送失败
    RCVoiceRoomRejectRequestSeatFailed(70000,"拒绝排麦请求发送失败"),
    /// 同步麦位信息失败
    RCVoiceRoomSyncSeatInfoFailed(70000,"同步麦位信息失败"),
    /// 同步房间信息失败
    RCVoiceRoomSyncRoomInfoFailed(70000,"同步房间信息失败"),
    /// 同步排麦相关信息
    RCVoiceRoomSyncRequestSeatFailed(70000,"同步排麦相关信息"),
    /// 获取排麦请求列表失败
    RCVoiceRoomGetRequestListFailed(70000,"获取排麦请求列表失败"),
    /// 发送信息失败
    RCVoiceRoomSendMessageFailed(70000,"发送信息失败"),
    /// 请求发送失败
    RCVoiceRoomSendInvitationSeatFailed(70000,"请求发送失败"),
    /// 取消请求发送失败
    RCVoiceRoomCancelInvitationFailed(70000,"取消请求发送失败"),
    /// 同意请求发送失败
    RCVoiceRoomAcceptInvitationFailed(70000,"同意请求发送失败"),
    /// 拒绝请求发送失败
    RCVoiceRoomRejectInvitationFailed(70000,"拒绝请求发送失败"),
    /// 创建语聊房失败
    RCVoiceRoomCreateRoomFailed(70000,"创建语聊房失败"),
    /// 用户ID为空
    RCVoiceRoomUserIdIsEmpty(70000,"用户ID为空"),
    
    UNKNOWN_ERROR(-1, "Unknown error"),
    SEAT_NOT_EXIST(1, "Seat not exist"),
    SEAT_IS_NOT_IDLE(2, "Seat is locked or using"),
    USER_IS_ON_SEAT_NOW(3, "User is on seat now"),
    SWITCH_ROLE_FAILED(4,"Failed to switch role"),
    USER_NOT_ON_SEAT_NOW(5,"User not on seat now"),
    TARGET_SEAT_NO_IN_RANGE(6,"Target seat not in range"),
    USER_CAN_NOT_PICK_SELF_ON_SEAT(7,"user can't pick self on seat"),
    USER_CAN_NOT_KICK_SELF(8,"user can't kick self"),
    JOIN_MIC_QUEUE_ALREADY(9,"Join mic queue already"),
    TOO_MUCH_PEOPLE_IN_MIC_QUEUE(10,"too much people in mic queue"),
    ;


    // 使用 map 提高查询效率
    private static final Map<Integer, VoiceRoomErrorCode> map = new HashMap<>();

    static {
        for (VoiceRoomErrorCode value : VoiceRoomErrorCode.values()) {
            map.put(value.code, value);
        }

         
    }

    private int code;
    private String message;

    VoiceRoomErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static VoiceRoomErrorCode valueOf(int code) {
        VoiceRoomErrorCode errorCode = map.get(code);
        return errorCode == null ? UNKNOWN_ERROR : errorCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
