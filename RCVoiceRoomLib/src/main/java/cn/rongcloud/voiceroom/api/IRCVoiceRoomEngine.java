/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import android.app.Application;

import java.util.List;

import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomEventListener;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomResultCallback;
import cn.rongcloud.voiceroom.model.AudioQuality;
import cn.rongcloud.voiceroom.model.AudioScenario;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.utils.BuildVersion;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.MessageContent;

/**
 * @author gusd
 * @Date 2021/07/30
 */
public interface IRCVoiceRoomEngine {
    /**
     * 设置房间事件回调
     *
     * @param listener 事件回调
     */
    public abstract void setVoiceRoomEventListener(RCVoiceRoomEventListener listener);

    /**
     * 增加其他消息的回调监听
     *
     * @param listener 消息回调 {@link IRongCoreListener.OnReceiveMessageListener}
     */
    public abstract void addMessageReceiveListener(IRongCoreListener.OnReceiveMessageListener listener);

    /**
     * 移除其他消息的回调监听
     *
     * @param listener 消息回调 {@link IRongCoreListener.OnReceiveMessageListener}
     */
    public abstract void removeMessageReceiveListener(IRongCoreListener.OnReceiveMessageListener listener);

    /**
     * 初始化 AppKey，如果已经初始化了 RongCoreClient 则不需调用此方法
     *
     * @param context 当前应用的 application
     * @param appKey  开发者申请的 AppKey
     */
    public abstract void initWithAppKey(Application context, String appKey);

    /**
     * 连接融云服务器，如果使用 RCCoreClient 连接过服务可不用调用此方法
     *
     * @param context  当前应用的 application
     * @param appToken 从服务器获取的 Token
     * @param callback 结果回调
     */
    public abstract void connectWithToken(Application context, String appToken, RCVoiceRoomCallback callback);

    /**
     * 创建并加入房间
     *
     * @param roomId   房间 Id
     * @param roomInfo 房间信息
     * @param callback 结果回调
     */
    public abstract void createAndJoinRoom(String roomId, RCVoiceRoomInfo roomInfo, RCVoiceRoomCallback callback);

    /**
     * 用户根据roomId加入一个语聊房
     *
     * @param roomId   房间 Id
     * @param callback 结果回调
     */
    public abstract void joinRoom(String roomId, RCVoiceRoomCallback callback);

    /**
     * 离开房间
     *
     * @param callback 结果回调
     */
    public abstract void leaveRoom(RCVoiceRoomCallback callback);

    /**
     * 用户主动上麦
     *
     * @param seatIndex 麦位序号
     * @param callback  结果回调
     */
    public abstract void enterSeat(int seatIndex, RCVoiceRoomCallback callback);

    /**
     * 用户主动下麦
     *
     * @param callback 结果回调
     */
    public abstract void leaveSeat(RCVoiceRoomCallback callback);

    /**
     * 用户跳麦，在用户已经在麦位想切换麦位时调用
     *
     * @param seatIndex 需要跳转的麦位序号
     * @param callback  结果回调
     */
    public abstract void switchSeatTo(int seatIndex, RCVoiceRoomCallback callback);

    /**
     * 抱用户上麦
     *
     * @param userId   用户 Id
     * @param callback 结果回调
     */
    public abstract void pickUserToSeat(String userId, RCVoiceRoomCallback callback);

    /**
     * 将某个麦位下麦
     *
     * @param userId   用户 Id
     * @param callback 结果回调
     */
    public abstract void kickUserFromSeat(String userId, RCVoiceRoomCallback callback);

    /**
     * 将某个用户踢出房间
     *
     * @param userId   用户 Id
     * @param callback 结果回调
     */
    public abstract void kickUserFromRoom(String userId, RCVoiceRoomCallback callback);

    /**
     * 锁定某个麦位
     *
     * @param seatIndex 麦位序号
     * @param isLocked  是否锁麦位
     * @param callback  结果回调
     */
    public abstract void lockSeat(int seatIndex, boolean isLocked, RCVoiceRoomCallback callback);

    /**
     * 将某个麦位静音
     *
     * @param seatIndex 麦位序号
     * @param isMute    是否静音
     * @param callback  结果回调
     */
    public abstract void muteSeat(int seatIndex, boolean isMute, RCVoiceRoomCallback callback);

    /**
     * 将所有麦位静音打开或者关闭
     *
     * @param isMute 是否静音
     */
    public abstract void muteOtherSeats(boolean isMute);

    /**
     * 静音所有远程音频流
     *
     * @param isMute 是否静音
     */
    public abstract void muteAllRemoteStreams(boolean isMute);

    /**
     * 将所有麦位所卖或者解除锁麦
     *
     * @param isLock
     */
    public abstract void lockOtherSeats(boolean isLock);

    /**
     * 发送消息
     *
     * @param message  融云消息实体
     * @param callback 结果回调
     */
    public abstract void sendMessage(MessageContent message, RCVoiceRoomCallback callback);

    /**
     * 设置房间信息，房间的id必须与当前房间id一致
     *
     * @param roomInfo 修改的房间信息 {@link RCVoiceRoomInfo}
     * @param callback 结果回调
     */
    public abstract void setRoomInfo(RCVoiceRoomInfo roomInfo, RCVoiceRoomCallback callback);

    /**
     * 停止本地麦克风收音
     *
     * @param isDisable 是否停止
     */
    public abstract void disableAudioRecording(boolean isDisable);

    /**
     * 设置房间音频质量和场景
     *
     * @param audioQuality 音频质量
     * @param scenario     音频场景
     */
    public abstract void setAudioQuality(AudioQuality audioQuality, AudioScenario scenario);

    /**
     * 是否使用扬声器
     *
     * @param isEnable 是否使用
     */
    public abstract void enableSpeaker(boolean isEnable);

    /**
     * 请求排麦
     *
     * @param callback 结果回调
     */
    public abstract void requestSeat(RCVoiceRoomCallback callback);

    /**
     * 取消排麦请求
     *
     * @param callback 结果回调
     */
    public abstract void cancelRequestSeat(RCVoiceRoomCallback callback);

    /**
     * 同意用户排麦请求
     *
     * @param userId   请求排麦的用户 Id
     * @param callback 结果回调
     */
    public abstract void acceptRequestSeat(String userId, RCVoiceRoomCallback callback);

    /**
     * 拒绝用户排麦请求
     *
     * @param userId
     * @param callback
     */
    public abstract void rejectRequestSeat(String userId, RCVoiceRoomCallback callback);

    /**
     * 获取当前排麦的用户列表
     *
     * @param callback 结果回调
     */
    public abstract void getRequestSeatUserIds(RCVoiceRoomResultCallback<List<String>> callback);

    /**
     * 发送请求
     *
     * @param content  发送的请求内容
     * @param callback 结果回调
     */
    public abstract void sendInvitation(String content, RCVoiceRoomResultCallback<String> callback);

    /**
     * 拒绝请求
     *
     * @param invitationId 请求的 Id
     * @param callback     结果回调
     */
    public abstract void rejectInvitation(String invitationId, RCVoiceRoomCallback callback);

    /**
     * 同意请求
     *
     * @param invitationId 请求 Id
     * @param callback     结果回调
     */
    public abstract void acceptInvitation(String invitationId, RCVoiceRoomCallback callback);

    /**
     * 取消自己发出的请求
     *
     * @param invitationId 请求 Id
     * @param callback     结果回调
     */
    public abstract void cancelInvitation(String invitationId, RCVoiceRoomCallback callback);

    /**
     * 通知房间所有用户执行某个刷新操作
     *
     * @param name    刷新操作的名称
     * @param content 刷新操作的内容
     */
    public abstract void notifyVoiceRoom(String name, String content);

    /**
     * 断开连接，用户退出时调用
     */
    public abstract void disConnect();


}
