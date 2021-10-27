/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.radioroom.room;

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
@MessageTag(value = "RC:VRLChatEnter", flag = MessageTag.NONE)
public class RCChatRoomEnter extends MessageContent {

    public static final Creator<RCChatRoomEnter> CREATOR = new Creator<RCChatRoomEnter>() {
        @Override
        public RCChatRoomEnter createFromParcel(Parcel source) {
            return new RCChatRoomEnter(source);
        }

        @Override
        public RCChatRoomEnter[] newArray(int size) {
            return new RCChatRoomEnter[size];
        }
    };
    private static final String TAG = "RCChatRoomEnter";
    private String userName;
    private String userId;

    public RCChatRoomEnter(byte[] data) {
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

    public RCChatRoomEnter() {
    }

    protected RCChatRoomEnter(Parcel in) {
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
