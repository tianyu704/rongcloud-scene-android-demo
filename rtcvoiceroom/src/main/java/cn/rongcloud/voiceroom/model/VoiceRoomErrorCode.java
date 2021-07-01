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
