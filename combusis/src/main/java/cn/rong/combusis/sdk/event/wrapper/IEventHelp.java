package cn.rong.combusis.sdk.event.wrapper;

import com.kit.wapper.IResultBack;

import java.util.List;

import cn.rong.combusis.sdk.event.listener.RoomListener;
import cn.rong.combusis.sdk.event.listener.StatusListener;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;

public interface IEventHelp {

    /**
     * 是否初始化
     *
     * @return
     */
    boolean isInitlaized();

    /**
     * 注册房间事件 加入房间前调用
     *
     * @param roomId
     */
    void regeister(String roomId);

    /**
     * 取消房间事件注册 退出房间后调用
     */
    void unregeister();

    /**
     * 添加房间事件监听
     *
     * @param listener 房间监听
     */
    void addRoomListener(RoomListener listener);

    /**
     * 添加网络延迟监听
     *
     * @param listener 网络监听
     */
    void addStatusListener(StatusListener listener);

    /**
     * 根据用户id获取麦位信息
     *
     * @param userId
     *
     * @return 麦位信息
     */
    RCVoiceSeatInfo getSeatInfo(String userId);

    /**
     * 根据用户index获取麦位信息
     *
     * @param index 索引
     *
     * @return
     */
    RCVoiceSeatInfo getSeatInfo(int index);

    /**
     * 获取房间在线用户id集合
     *
     * @param roomId     房间
     * @param resultBack
     */
    void getOnLineUserIds(String roomId, IResultBack<List<String>> resultBack);

    /**
     * 获取未读消息数据
     *
     * @param roomId
     * @param resultBack
     */
    void getUnReadMegCount(String roomId, IResultBack<Integer> resultBack);

    /**
     * 获取申请麦位用户id
     *
     * @param resultBack
     */
    void getRequestSeatUserIds(IResultBack<List<String>> resultBack);

    /**
     * 获取可用麦位索引
     *
     * @return 可用麦位索引
     */
    int getAvailableSeatIndex();

    /**
     * 获取当前pk邀请者的信息
     */
    PKInviter getPKInviter();

    /**
     * 释放PK邀请者
     */
    void releasePKInviter();

    /**
     * pk邀请者信息
     * 1.接收到pk邀请保存
     * 2.邀请被取消释放
     * 3.响应pk邀请时释放（手动）
     */
    class PKInviter {
        public String inviterRoomId;
        public String inviterId;
    }

    /**
     * pk中 被邀请者信息
     * 1.发起邀请api是保存
     * 2.取消邀请时释放（手动）
     * 3.接收到邀请响应释放
     */
    class PKInvitee {
        public String inviteeRoomId;
        public String inviteeId;
    }
}