package cn.rongcloud.voiceroom.sdk;

import android.text.TextUtils;

import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;

import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.event.wrapper.EToast;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;

public class VoiceRoomApi implements Api {
    private final static String TAG = "VoiceRoomApi";
    private final static Api api = new VoiceRoomApi();
    private final RCVoiceRoomInfo roomInfo = new RCVoiceRoomInfo();

    private VoiceRoomApi() {
    }

    public static Api getApi() {
        return api;
    }

    @Override
    public RCVoiceRoomInfo getRoomInfo() {
        return roomInfo;
    }

    /**
     * 邀请同意 上麦
     *
     * @param userId
     * @param resultBack
     */
    public void invitedEnterSeat(String userId, IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().pickUserToSeat(
                userId, new DefaultRoomCallback("invitedIntoSeat", "邀请上麦", resultBack));
    }


    /**
     * 房间通知
     *
     * @param name
     * @param content
     */
    @Override
    public void notifyRoom(String name, String content) {
        RCVoiceRoomEngine.getInstance().notifyVoiceRoom(name, content);
    }

    /**
     * 创建并加入房间
     *
     * @param roomId     房间id
     * @param roomInfo   房间实体
     * @param resultBack 回调
     */
    @Override
    public void createAndJoin(String roomId, RCVoiceRoomInfo roomInfo, IResultBack<Boolean> resultBack) {
        if (TextUtils.isEmpty(roomId)) {
            if (null != resultBack) resultBack.onResult(false);
            return;
        }
        if (null == roomInfo || TextUtils.isEmpty(roomInfo.getRoomName()) || roomInfo.getSeatCount() < 1) {
            if (null != resultBack) resultBack.onResult(false);
            return;
        }
        RCVoiceRoomEngine.getInstance().createAndJoinRoom(roomId, roomInfo,
                new DefaultRoomCallback(
                        "createAndJoin",
                        "创建并加入房间",
                        resultBack));
    }

    @Override
    public void joinRoom(String roomId, IResultBack<Boolean> resultBack) {
        if (TextUtils.isEmpty(roomId)) {
            if (null != resultBack) resultBack.onResult(false);
            return;
        }
        RCVoiceRoomEngine.getInstance().joinRoom(roomId,
                new DefaultRoomCallback(
                        "joinRoom",
                        "加入房间",
                        resultBack));
    }

    @Override
    public void leaveRoom(IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().leaveRoom(
                new DefaultRoomCallback(
                        "leaveRoom",
                        "离开房间",
                        resultBack));
    }

    @Override
    public void lockAll(boolean locked) {
        RCVoiceRoomEngine.getInstance().lockOtherSeats(locked);
        EToast.showToastWithLag(TAG, locked ? "全麦锁定成功" : "全麦解锁成功");
    }

    @Override
    public void muteAll(boolean mute) {
        RCVoiceRoomEngine.getInstance().muteOtherSeats(mute);
        EToast.showToastWithLag(TAG, mute ? "全麦静音成功" : "全麦取消静音成功");
    }

    /**
     * 锁麦
     *
     * @param index
     * @param locked
     * @param resultBack
     */
    @Override
    public void lockSeat(int index, boolean locked, IResultBack<Boolean> resultBack) {
        String action = locked ? "麦位锁定" : "取消麦位解锁";
        RCVoiceRoomEngine.getInstance().lockSeat(
                index,
                locked,
                new DefaultRoomCallback(
                        "muteSeat",
                        action,
                        resultBack));
    }

    /**
     * 静麦
     *
     * @param index
     * @param mute
     * @param resultBack
     */
    @Override
    public void muteSeat(int index, boolean mute, IResultBack<Boolean> resultBack) {
        String action = mute ? "麦位静音" : "取消麦位静音";
        RCVoiceRoomEngine.getInstance().muteSeat(index, mute,
                new DefaultRoomCallback(
                        "muteSeat",
                        action,
                        resultBack));
    }

    /**
     * 下麦
     *
     * @param resultBack
     */
    @Override
    public void leaveSeat(IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().leaveSeat(
                new DefaultRoomCallback(
                        "leaveSeat",
                        "下麦",
                        resultBack));
    }

    /**
     * 上麦
     *
     * @param index
     * @param resultBack
     */
    @Override
    public void enterSeat(int index, IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().enterSeat(
                index,
                new DefaultRoomCallback(
                        "enterSeat",
                        "上麦",
                        resultBack));
    }

    /**
     * 申请上麦
     *
     * @param resultBack
     */
    @Override
    public void requestSeat(IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().requestSeat(
                new DefaultRoomCallback(
                        "requestSeat",
                        "申请上麦",
                        resultBack));
    }

    /**
     * 撤销上麦申请
     *
     * @param resultBack
     */
    @Override
    public void cancelRequestSeat(IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().cancelRequestSeat(
                new DefaultRoomCallback("cancelRequestSeat", "取消排麦", resultBack));
    }

    /**
     * 管理员/房主：同意上麦申请
     *
     * @param userId
     * @param resultBack
     */
    @Override
    public void acceptRequestSeat(String userId, IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().acceptRequestSeat(userId,
                new DefaultRoomCallback("acceptRequestSeat", "同意排麦请求", resultBack));
    }

    @Override
    public void rejectRequestSeat(String userId, IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().rejectRequestSeat(userId,
                new DefaultRoomCallback("rejectRequestSeat", "拒绝排麦请求", resultBack));
    }

    @Override
    public void updateSeatExtra(int seatIndex, String extra, IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().updateSeatInfo(seatIndex, extra,
                new DefaultRoomCallback("updateSeatExtra", "更新扩展属性", resultBack));
    }

    @Override
    public void updateSeatCount(int count, IResultBack<Boolean> resultBack) {
        roomInfo.setSeatCount(count);
        updateRoomInfo(roomInfo, resultBack);
    }

    @Override
    public void updateRoomName(String name, IResultBack<Boolean> resultBack) {
        roomInfo.setRoomName(name);
        updateRoomInfo(roomInfo, resultBack);
    }

    /**
     * 为避免重置未修改属性，建议跟新和创建时传入相同的对象，
     *
     * @param roomInfo
     * @param resultBack
     */
    private void updateRoomInfo(RCVoiceRoomInfo roomInfo, IResultBack<Boolean> resultBack) {
        RCVoiceRoomEngine.getInstance().setRoomInfo(roomInfo, new DefaultRoomCallback("updateRoomInfo", resultBack));
    }

}
