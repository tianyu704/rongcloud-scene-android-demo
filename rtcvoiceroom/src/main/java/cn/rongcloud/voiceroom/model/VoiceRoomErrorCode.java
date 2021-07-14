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
    Unknown_Error(-1,"Unknown error"),
    /// 操作成功
    RCVoiceRoomSuccess(70000,"操作成功"),
    /// 连接服务器失败
    RCVoiceRoomConnectTokenFailed(70001,"Init token failed"),
    /// 麦位序号不对
    RCVoiceRoomSeatIndexOutOfRange(70000,"Seat index not correct"),
    /// 用户已经在麦位上
    RCVoiceRoomUserAlreadyOnSeat(70000,"User is on seat now"),
    /// 用户不在麦位上
    RCVoiceRoomUserNotOnSeat(70000,"User Not on seat now"),
    /// 用户跳麦的麦位和之前的一样
    RCVoiceRoomJumpIndexEqual(70000,"Target index can't equal to current index"),
    /// 麦位不是空置状态
    RCVoiceRoomSeatNotEmpty(70000,"Seat is locked or using"),
    /// 不能抱自己上麦
    RCVoiceRoomPickSelfToSeat(70000,"User can't pick self on seat"),
    /// 发送抱麦请求失败
    RCVoiceRoomPickUserFailed(70000,"Pick user seat failed"),
    /// 不能踢自己下麦
    RCVoiceRoomUserKickSelfFromSeat(70000,"User can't kick self"),
    /// 加入语聊房失败
    RCVoiceRoomJoinRoomFailed(70000,"Join ChatRoom Failed"),
    /// 离开语聊房失败
    RCVoiceRoomLeaveRoomFailed(70000,"Leave chat or rtc room failed"),
    /// 获取房间信息失败
    RCVoiceRoomGetRoomInfoFailed(70000,"获取房间信息失败"),
    /// 已经在排麦列表中了
    RCVoiceRoomAlreadyInRequestList(70000,"User already on seat"),
    /// 排麦人数太多
    RCVoiceRoomRequestListFull(70000,"Request seat list is full, the max is 20"),
    /// 排麦请求发送失败
    RCVoiceRoomSendRequestSeatFailed(70000,"Send request seat failed"),
    /// 取消排麦请求发送失败
    RCVoiceRoomCancelRequestSeatFailed(70000,"Cancel request seat failed"),
    /// 同意排麦请求发送失败
    RCVoiceRoomAcceptRequestSeatFailed(70000,"同意排麦请求发送失败"),
    /// 拒绝排麦请求发送失败
    RCVoiceRoomRejectRequestSeatFailed(70000,"拒绝排麦请求发送失败"),
    /// 同步麦位信息失败
    RCVoiceRoomSyncSeatInfoFailed(70000,"sync seat info failed"),
    /// 同步房间信息失败
    RCVoiceRoomSyncRoomInfoFailed(70000,"setup Room Info failed"),
    /// 同步排麦相关信息
    RCVoiceRoomSyncRequestSeatFailed(70000,"update waiting kv failed"),
    /// 获取排麦请求列表失败
    RCVoiceRoomGetRequestListFailed(70000,"Get entries failed"),

    /// 发送信息失败
    RCVoiceRoomSendMessageFailed(70000,"Send message failed"),
    /// 请求发送失败
    RCVoiceRoomSendInvitationSeatFailed(70000,"send invitation failed"),
    /// 取消请求发送失败
    RCVoiceRoomCancelInvitationFailed(70000,"cancel invitation failed"),
    /// 同意请求发送失败
    RCVoiceRoomAcceptInvitationFailed(70000,"accept invitation failed"),
    /// 拒绝请求发送失败
    RCVoiceRoomRejectInvitationFailed(70000,"reject invitation failed"),
    /// 创建语聊房失败
    RCVoiceRoomCreateRoomFailed(70000,"Create room failed"),
    /// 用户ID为空
    RCVoiceRoomUserIdIsEmpty(70000,"Room or User Id is Empty");


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
        return errorCode == null ? Unknown_Error : errorCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
