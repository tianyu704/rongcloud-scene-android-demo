/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 语聊房麦位封装实体
 */
public class RCVoiceSeatInfo extends BaseInfo implements Parcelable, Cloneable {

    public RCVoiceSeatInfo() {
    }

    /**
     * 当前状态
     */
    @SerializedName("status")
    private RCSeatStatus mStatus = RCSeatStatus.RCSeatStatusEmpty;

    /**
     * 是否静音
     */
    @SerializedName("mute")
    private boolean mute;

    /**
     * 正在发言
     */
    @SerializedName("speaking")
    private boolean speaking;

    /**
     * 用户 Id
     */
    @SerializedName("userId")
    private String userId;

    /**
     * 拓展
     */
    @SerializedName("extra")
    private String extra;


    public RCSeatStatus getStatus() {
        return mStatus;
    }

    public void setStatus(RCSeatStatus status) {
        mStatus = status;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void setSpeaking(boolean speaking) {
        this.speaking = speaking;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    /**
     * 麦位状态
     */
    public enum RCSeatStatus {
        /**
         * 麦位空闲
         */
        @SerializedName("0")
        RCSeatStatusEmpty,

        /**
         * 麦位占用中
         */
        @SerializedName("1")
        RCSeatStatusUsing,

        /**
         * 麦位被锁
         */
        @SerializedName("2")
        RCSeatStatusLocking
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus == null ? -1 : this.mStatus.ordinal());
        dest.writeByte(this.mute ? (byte) 1 : (byte) 0);
        dest.writeByte(this.speaking ? (byte) 1 : (byte) 0);
        dest.writeString(this.userId);
        dest.writeString(this.extra);
    }

    public void readFromParcel(Parcel source) {
        int tmpMStatus = source.readInt();
        this.mStatus = tmpMStatus == -1 ? null : RCSeatStatus.values()[tmpMStatus];
        this.mute = source.readByte() != 0;
        this.speaking = source.readByte() != 0;
        this.userId = source.readString();
        this.extra = source.readString();
    }


    protected RCVoiceSeatInfo(Parcel in) {
        int tmpMStatus = in.readInt();
        this.mStatus = tmpMStatus == -1 ? null : RCSeatStatus.values()[tmpMStatus];
        this.mute = in.readByte() != 0;
        this.speaking = in.readByte() != 0;
        this.userId = in.readString();
        this.extra = in.readString();
    }

    public static final Creator<RCVoiceSeatInfo> CREATOR = new Creator<RCVoiceSeatInfo>() {
        @Override
        public RCVoiceSeatInfo createFromParcel(Parcel source) {
            return new RCVoiceSeatInfo(source);
        }

        @Override
        public RCVoiceSeatInfo[] newArray(int size) {
            return new RCVoiceSeatInfo[size];
        }
    };

    // 浅拷贝即可
    public RCVoiceSeatInfo clone() {
        try {
            return (RCVoiceSeatInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String toString() {
        return "RCVoiceSeatInfo{" +
                "mStatus=" + mStatus +
                ", mute=" + mute +
                ", speaking=" + speaking +
                ", userId='" + userId + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }

}
