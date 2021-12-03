package cn.rongcloud.liveroom.helper;


import android.view.View;

import java.util.List;

import cn.rong.combusis.provider.user.User;
import cn.rongcloud.liveroom.api.RCLiveEventListener;
import cn.rongcloud.liveroom.api.RCLiveSeatInfo;
import cn.rongcloud.liveroom.api.RCParamter;
import cn.rongcloud.liveroom.api.SeatViewProvider;

/**
 * @author lihao
 * @project RongRTCDemo
 * @date 2021/11/16
 * @time 5:30 下午
 */
public interface LiveRoomListener extends RCLiveEventListener {

    /**
     * 申请上麦的用户
     *
     * @param requestLives
     */
    void onRequestLiveVideoIds(List<String> requestLives);


    /**
     * 可以被邀请的用户
     */
    void onInvitateLiveVideoIds(List<User> roomUsers);

    View setSeatViewProvider(RCLiveSeatInfo seatInfo, RCParamter rcParamter);
}
