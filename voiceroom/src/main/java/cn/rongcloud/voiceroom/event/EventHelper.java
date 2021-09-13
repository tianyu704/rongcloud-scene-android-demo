package cn.rongcloud.voiceroom.event;

import android.app.Activity;

import com.kit.wapper.IResultBack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cn.rong.combusis.provider.user.UserProvider;
import cn.rongcloud.voiceroom.event.wrapper.AbsEvenHelper;
import cn.rongcloud.voiceroom.event.wrapper.EventDialogHelper;
import cn.rongcloud.voiceroom.event.wrapper.IEventHelp;
import cn.rongcloud.voiceroom.event.listener.NetStatusListener;
import cn.rongcloud.voiceroom.event.listener.RoomListener;
import cn.rongcloud.voiceroom.event.wrapper.TipType;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import io.rong.imlib.model.UserInfo;

public class EventHelper extends AbsEvenHelper {

    private final static IEventHelp _helper = new EventHelper();
    private WeakReference<Activity> activity;

    private EventHelper() {
    }

    public static IEventHelp helper() {
        return _helper;
    }

    public boolean isInitlaized() {
        return null != activity && null != activity.get();
    }

    public void regeister(Activity activity) {
        this.activity = new WeakReference<>(activity);
        init();
    }

    @Override
    public void unregeister() {
        if (null != activity) {
            activity.clear();
        }
        activity = null;
        unInit();
    }

    @Override
    public void addRoomListener(RoomListener listener) {
        if (null == listeners) listeners = new ArrayList<>();
        listeners.add(listener);
    }

    @Override
    public void addStatusListener(NetStatusListener listener) {
        if (null == statusListeners) statusListeners = new ArrayList<>();
        statusListeners.add(listener);
    }

    /**
     * 根据用户id获取麦位信息
     *
     * @param userId
     *
     * @return 麦位信息
     */
    public RCVoiceSeatInfo getSeatInfo(String userId) {
        synchronized (obj) {
            int count = mSeatInfos.size();
            for (int i = 0; i < count; i++) {
                RCVoiceSeatInfo s = mSeatInfos.get(i);
                if (userId.equals(s.getUserId())) {
                    return s;
                }
            }
            return null;
        }
    }

    /**
     * 获取可用麦位索引
     *
     * @return 可用麦位索引
     */
    public int getAvailableSeatIndex() {
        synchronized (obj) {
            int availableIndex = -1;
            for (int i = 0; i < mSeatInfos.size(); i++) {
                RCVoiceSeatInfo seat = mSeatInfos.get(i);
                if (RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty == seat.getStatus()) {
                    availableIndex = i;
                    break;
                }
            }
            return availableIndex;
        }
    }


    @Override
    protected void onShowTipDialog(String userId, TipType type, IResultBack<Boolean> resultBack) {
        if (null == activity || null == activity.get()) {
            if (null != resultBack) resultBack.onResult(false);
            return;
        }
        // 根据userId获取用户信息
        UserProvider.provider().getAsyn(userId, new IResultBack<UserInfo>() {
            @Override
            public void onResult(UserInfo userInfo) {
                String message = "";
                if (null != userInfo) {
                    if (TipType.InvitedSeat == type) {
                        message = userInfo.getName() + "邀请您上麦，是否同意？";
                    } else if (TipType.RequestSeat == type) {
                        message = userInfo.getName() + "申请上麦，是否同意？";
                    } else {
                        message = userInfo.getName() + "邀请您进行PK，是否同意？";
                    }
                    EventDialogHelper.helper().showTipDialog(activity.get(), type.getValue(), message, resultBack);
                } else {
                    if (null != resultBack) resultBack.onResult(false);
                }
            }
        });

    }
}