/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * 语聊房封装实体
 */
public class RCVoiceRoomInfo extends BaseInfo implements Parcelable, Cloneable {

    /**
     * 房间名
     */
    @SerializedName("roomName")
    private String mRoomName;
    /**
     * 房间座位数
     */
    @SerializedName("seatCount")
    private int mSeatCount;

    /**
     * 是否可以自由上麦，状态标记，直接修改不会自动触发锁麦操作
     */
    @SerializedName("isFreeEnterSeat")
    private boolean isFreeEnterSeat;

    /**
     * 房间麦位锁定状态，状态标记，直接修改不会自动触发锁麦操作
     */
    @SerializedName("isLockAll")
    private boolean isLockAll;

    /**
     * 房间麦位静音状态，状态标记，直接修改不会自动触发静音麦位操作
     */
    @SerializedName("isMuteAll")
    private boolean isMuteAll;

    /**
     * 拓展字段
     */
    @SerializedName("extra")
    private String extra;

    public String getRoomName() {
        return mRoomName;
    }

    public void setRoomName(String roomName) {
        mRoomName = roomName;
    }

    public int getSeatCount() {
        return mSeatCount;
    }

    public void setSeatCount(int seatCount) {
        mSeatCount = seatCount;
    }


    public boolean isFreeEnterSeat() {
        return isFreeEnterSeat;
    }

    public void setFreeEnterSeat(boolean freeEnterSeat) {
        isFreeEnterSeat = freeEnterSeat;
    }


    public String getExtra() {
        return extra;
    }

    public boolean isLockAll() {
        return isLockAll;
    }

    public void setLockAll(boolean lockAll) {
        isLockAll = lockAll;
    }

    public boolean isMuteAll() {
        return isMuteAll;
    }

    public void setMuteAll(boolean muteAll) {
        isMuteAll = muteAll;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "RCVoiceRoomInfo{" +
                "mRoomName='" + mRoomName + '\'' +
                ", mSeatCount=" + mSeatCount +
                ", isFreeEnterSeat=" + isFreeEnterSeat +
                ", isLockAll=" + isLockAll +
                ", isMuteAll=" + isMuteAll +
                ", extra='" + extra + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCVoiceRoomInfo that = (RCVoiceRoomInfo) o;
        return mSeatCount == that.mSeatCount &&
                isFreeEnterSeat == that.isFreeEnterSeat &&
                isLockAll == that.isLockAll &&
                isMuteAll == that.isMuteAll &&
                Objects.equals(mRoomName, that.mRoomName) &&
                Objects.equals(extra, that.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mRoomName, mSeatCount, isFreeEnterSeat, isLockAll, isMuteAll, extra);
    }


    public RCVoiceRoomInfo() {
    }


    public RCVoiceRoomInfo clone() {
        try {
            return (RCVoiceRoomInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mRoomName);
        dest.writeInt(this.mSeatCount);
        dest.writeByte(this.isFreeEnterSeat ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isLockAll ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isMuteAll ? (byte) 1 : (byte) 0);
        dest.writeString(this.extra);
    }

    public void readFromParcel(Parcel source) {
        this.mRoomName = source.readString();
        this.mSeatCount = source.readInt();
        int tmpMAudioQuality = source.readInt();
        this.isFreeEnterSeat = source.readByte() != 0;
        int tmpMScenario = source.readInt();
        this.isLockAll = source.readByte() != 0;
        this.isMuteAll = source.readByte() != 0;
        this.extra = source.readString();
    }

    protected RCVoiceRoomInfo(Parcel in) {
        this.mRoomName = in.readString();
        this.mSeatCount = in.readInt();
        int tmpMAudioQuality = in.readInt();
        this.isFreeEnterSeat = in.readByte() != 0;
        int tmpMScenario = in.readInt();
        this.isLockAll = in.readByte() != 0;
        this.isMuteAll = in.readByte() != 0;
        this.extra = in.readString();
    }

    public static final Creator<RCVoiceRoomInfo> CREATOR = new Creator<RCVoiceRoomInfo>() {
        @Override
        public RCVoiceRoomInfo createFromParcel(Parcel source) {
            return new RCVoiceRoomInfo(source);
        }

        @Override
        public RCVoiceRoomInfo[] newArray(int size) {
            return new RCVoiceRoomInfo[size];
        }
    };
}
