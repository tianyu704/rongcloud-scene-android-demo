/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import cn.rongcloud.voiceroom.utils.BuildVersion;

/**
 * 语聊房对外提供的功能类接口
 */
public abstract class RCVoiceRoomEngine implements IRCVoiceRoomEngine {

    public static final String RC_REQUEST_SEAT_PREFIX_KEY = "RCRequestSeatPrefixKey";
    public static final String RC_REQUEST_SEAT_CONTENT_REQUEST = "RCRequestSeatContentRequest";
    public static final String RC_REQUEST_SEAT_CONTENT_ACCEPT = "RCRequestSeatContentAccept";
    public static final String RC_REQUEST_SEAT_CONTENT_CANCELLED = "RCRequestSeatContentCancelled";
    public static final String RC_REQUEST_SEAT_CONTENT_DENY = "RCRequestSeatContentDeny";
    public static final String RC_KICK_USER_OUT_ROOM_CONTENT = "RCKickUserOutRoomContent";
    public static final String RC_ROOM_INFO_KEY = "RCRoomInfoKey";
    public static final String RC_PICKER_USER_SEAT_CONTENT = "RCPickerUserSeatContent";
    public static final String RC_AUDIENCE_JOIN_ROOM = "RCAudienceJoinRoom";
    public static final String RC_AUDIENCE_LEAVE_ROOM = "RCAudienceLeaveRoom";
    public static final String RC_USER_ON_SEAT_SPEAKING_KEY = "RCUserOnSeatSpeakingKey";
    public static final String RC_ON_USER_LEAVE_SEAT_EVENT_PREFIX_KEY = "RCOnUserLeaveSeatHappenedPrefixKey";

    public static final String RC_SEAT_INFO_USER_PART_PREFIX_KEY = "RCSeatInfoUserPartPrefixKey";
    public static final String RC_SEAT_INFO_SEAT_PART_PREFIX_KEY = "RCSeatInfoSeatPartPrefixKey";

    public static IRCVoiceRoomEngine getInstance() {
        return RCVoiceRoomEngineProxy.getInstance();
    }

    /**
     * 获取 SDK 版本号
     *
     * @return 版本号
     */
    public static String getVersion() {
        return BuildVersion.SDK_VERSION;
    }
}
