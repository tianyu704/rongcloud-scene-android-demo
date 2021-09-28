package cn.rongcloud.radioroom.ui.room;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.basis.net.LoadTag;
import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomLike;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.room.AbsRoomActivity;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.callback.RCRadioRoomCallback;
import cn.rongcloud.radioroom.rroom.RCRadioRoomInfo;
import cn.rongcloud.rtc.base.RCRTCLiveRole;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomActivity extends AbsRoomActivity<VoiceRoomBean> {
    private static final String KEY_ROOM_IDS = "KEY_ROOM_IDS";
    private static final String KEY_ROOM_POSITION = "KEY_ROOM_POSITION";
    private LoadTag mLoadTag;

    public static void startActivity(Activity activity, ArrayList<String> roomIds, int position) {
        Intent intent = new Intent(activity, RadioRoomActivity.class);
        intent.putStringArrayListExtra(KEY_ROOM_IDS, roomIds);
        intent.putExtra(KEY_ROOM_POSITION, position);
        activity.startActivity(intent);
    }

    @Override
    protected void initRoom() {
        mLoadTag = new LoadTag(activity, "Loading...");
        RCMessager.getInstance().addMessageTypes(
                RCChatroomEnter.class,
                RCChatroomBarrage.class,
                RCChatroomGift.class,
                RCChatroomGiftAll.class,
                RCChatroomLike.class);
    }

    @Override
    protected int getCurrentItem() {
        if (getIntent().hasExtra(KEY_ROOM_POSITION)) {
            return getIntent().getIntExtra(KEY_ROOM_POSITION, 0);
        }
        return 0;
    }

    @Override
    public Fragment getFragment() {
        return RadioRoomFragment.getInstance();
    }

    @Override
    public List<String> loadData() {
        if (getIntent().hasExtra(KEY_ROOM_IDS)) {
            ArrayList<String> ids = getIntent().getStringArrayListExtra(KEY_ROOM_IDS);
            return ids;
        }
        return null;
    }

    @Override
    protected void switchRoom(String roomId) {
        mLoadTag.show();
        // 先退出上个房间
        RCRadioRoomEngine.getInstance().leaveRoom(new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                Logger.e("==============leaveRoom onSuccess");
                joinRadioRoom(roomId);
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("==============leaveRoom onError,code:" + code + ",message:" + message);
                joinRadioRoom(roomId);
            }
        });
    }

    private void joinRadioRoom(String roomId) {
        VoiceRoomProvider.provider().getAsyn(roomId, voiceRoomBean -> {
            RCRadioRoomInfo roomInfo = new RCRadioRoomInfo(RCRTCLiveRole.BROADCASTER);
            roomInfo.setRoomId(voiceRoomBean.getRoomId());
            roomInfo.setRoomName(voiceRoomBean.getRoomName());
            RCRadioRoomEngine.getInstance().joinRoom(roomInfo, new RCRadioRoomCallback() {
                @Override
                public void onSuccess() {
                    Logger.e("==============joinRoom onSuccess");
                    joinRoom(voiceRoomBean);
                    mLoadTag.dismiss();
                }

                @Override
                public void onError(int code, String message) {
                    Logger.e("==============joinRoom onError,code:" + code + ",message:" + message);
                    mLoadTag.dismiss();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
