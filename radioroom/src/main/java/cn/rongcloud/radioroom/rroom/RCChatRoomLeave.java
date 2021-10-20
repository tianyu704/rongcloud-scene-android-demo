/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.radioroom.rroom;

import android.os.Parcel;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 刷新消息
 */
@MessageTag(value = "RC:VRLChatLeave", flag = MessageTag.NONE)
public class RCChatRoomLeave extends MessageContent {

    public static final Creator<RCChatRoomLeave> CREATOR = new Creator<RCChatRoomLeave>() {
        @Override
        public RCChatRoomLeave createFromParcel(Parcel source) {
            return new RCChatRoomLeave(source);
        }

        @Override
        public RCChatRoomLeave[] newArray(int size) {
            return new RCChatRoomLeave[size];
        }
    };
    private static final String TAG = "RCChatRoomLeave";
    private String userName;
    private String userId;

    public RCChatRoomLeave(byte[] data) {
        super(data);
        String jsonStr = null;
        jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("userName")) {
                userName = jsonObj.getString("userName");
            }
            if (jsonObj.has("userId")) {
                userId = jsonObj.getString("userId");
            }
        } catch (JSONException e) {
            RLog.e(TAG, "JSONException " + e.getMessage());
        }
    }

    public RCChatRoomLeave() {
    }

    protected RCChatRoomLeave(Parcel in) {
        this.userName = in.readString();
        this.userId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userName);
        dest.writeString(this.userId);
    }

    public void readFromParcel(Parcel source) {
        this.userName = source.readString();
        this.userId = source.readString();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            if (!TextUtils.isEmpty(userName)) {
                jsonObj.put("userName", userName);
            }
            if (!TextUtils.isEmpty(userId)) {
                jsonObj.put("userId", userId);
            }
            return jsonObj.toString().getBytes(StandardCharsets.UTF_8);
        } catch (JSONException e) {
            RLog.e(TAG, "JSONException " + e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return "RCVoiceRoomRefreshMessage{" +
                "userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
