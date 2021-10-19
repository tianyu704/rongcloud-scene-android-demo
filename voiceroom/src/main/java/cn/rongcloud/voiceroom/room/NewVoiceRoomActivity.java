package cn.rongcloud.voiceroom.room;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.ui.room.AbsRoomActivity;
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
