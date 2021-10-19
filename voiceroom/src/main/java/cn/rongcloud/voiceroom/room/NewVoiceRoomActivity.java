package cn.rongcloud.voiceroom.room;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomFollow;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.message.RCChatroomLike;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.message.RCChatroomSeats;
import cn.rong.combusis.message.RCChatroomUserBan;
import cn.rong.combusis.message.RCChatroomUserBlock;
import cn.rong.combusis.message.RCChatroomUserUnBan;
import cn.rong.combusis.message.RCChatroomUserUnBlock;
import cn.rong.combusis.message.RCChatroomVoice;
import cn.rong.combusis.message.RCFollowMsg;
import cn.rong.combusis.ui.room.AbsRoomActivity;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * @author 李浩  语聊房重构
 * @date 2021/9/24
 */
@AndroidEntryPoint
public class NewVoiceRoomActivity extends AbsRoomActivity {
    private static final String KEY_ROOM_IDS = "KEY_ROOM_IDS";
    public static final String ISCREATE = "ROOM_IS_CREATE";
    private static final String KEY_ROOM_POSITION = "KEY_ROOM_POSITION";
    private boolean isCreate;

    public static void startActivity(Activity activity, ArrayList<String> roomIds, int position, boolean isCreate) {
        Intent intent = new Intent(activity, NewVoiceRoomActivity.class);
        intent.putStringArrayListExtra(KEY_ROOM_IDS, roomIds);
        intent.putExtra(KEY_ROOM_POSITION, position);
        intent.putExtra(ISCREATE, isCreate);
        activity.startActivity(intent);
    }


    @Override
    protected void initRoom() {
        RCMessager.getInstance().addMessageTypes(
                RCChatroomAdmin.class,
                RCChatroomBarrage.class,
                RCChatroomEnter.class,
                RCChatroomFollow.class,
                RCChatroomGift.class,
                RCChatroomGiftAll.class,
                RCChatroomKickOut.class,
                RCChatroomLocationMessage.class,
                RCChatroomSeats.class,
                RCChatroomUserBan.class,
                RCChatroomUserBlock.class,
                RCChatroomUserUnBan.class,
                RCChatroomUserUnBlock.class,
                RCChatroomLike.class,
                RCChatroomVoice.class,
                RCFollowMsg.class);
        isCreate = getIntent().getBooleanExtra(ISCREATE, false);
    }


    @Override
    protected int getCurrentItem() {
        if (getIntent().hasExtra(KEY_ROOM_POSITION)) {
            return getIntent().getIntExtra(KEY_ROOM_POSITION, 0);
        }
        return 0;
    }


    @Override
    public Fragment getFragment(String roomId) {
        return NewVoiceRoomFragment.getInstance(roomId, isCreate);
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
    protected void onDestroy() {
        super.onDestroy();
        RCVoiceRoomEngine.getInstance().leaveRoom(new RCVoiceRoomCallback() {

            @Override
            public void onSuccess() {
                Logger.e("==============leaveRoom onSuccess");
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("==============leaveRoom onError");
            }
        });
    }


}
