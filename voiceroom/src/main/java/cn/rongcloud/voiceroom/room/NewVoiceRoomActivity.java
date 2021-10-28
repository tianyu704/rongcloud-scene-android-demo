package cn.rongcloud.voiceroom.room;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.intent.IntentWrap;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.ui.room.AbsRoomActivity;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;

/**
 * @author 李浩  语聊房重构
 * @date 2021/9/24
 */
public class NewVoiceRoomActivity extends AbsRoomActivity {
    private boolean isCreate;

    public static void startActivity(Activity activity, ArrayList<String> roomIds, int position, boolean isCreate) {
        IntentWrap.launchVoiceRoom(activity, roomIds, position, isCreate);
    }


    @Override
    protected void initRoom() {
        isCreate = getIntent().getBooleanExtra(IntentWrap.KEY_IS_CREATE, false);
    }


    @Override
    protected int getCurrentItem() {
        if (getIntent().hasExtra(IntentWrap.KEY_ROOM_POSITION)) {
            return getIntent().getIntExtra(IntentWrap.KEY_ROOM_POSITION, 0);
        }
        return 0;
    }


    @Override
    public Fragment getFragment(String roomId) {
        return NewVoiceRoomFragment.getInstance(roomId, isCreate);
    }

    @Override
    public List<String> loadData() {
        if (getIntent().hasExtra(IntentWrap.KEY_ROOM_IDS)) {
            ArrayList<String> ids = getIntent().getStringArrayListExtra(IntentWrap.KEY_ROOM_IDS);
            return ids;
        }
        return null;
    }

    @Override
    protected RoomType getRoomType() {
        return RoomType.VOICE_ROOM;
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
