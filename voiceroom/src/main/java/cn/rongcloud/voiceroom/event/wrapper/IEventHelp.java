package cn.rongcloud.voiceroom.event.wrapper;

import android.app.Activity;

import cn.rongcloud.voiceroom.event.listener.NetStatusListener;
import cn.rongcloud.voiceroom.event.listener.RoomListener;
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
     * @param activity
     */
    void regeister(Activity activity);

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
    void addStatusListener(NetStatusListener listener);

    /**
     * 根据用户id获取麦位信息
     *
     * @param userId
     *
     * @return 麦位信息
     */
    RCVoiceSeatInfo getSeatInfo(String userId);

    /**
     * 获取可用麦位索引
     *
     * @return 可用麦位索引
     */
    int getAvailableSeatIndex();

}