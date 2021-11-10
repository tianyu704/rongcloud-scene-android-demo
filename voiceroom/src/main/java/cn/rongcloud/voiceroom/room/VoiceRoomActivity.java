package cn.rongcloud.voiceroom.room;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import cn.rong.combusis.intent.IntentWrap;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.ui.room.AbsRoomActivity;

/**
 * @author 李浩  语聊房重构
 * @date 2021/9/24
 */
public class VoiceRoomActivity extends AbsRoomActivity {
    private boolean isCreate;

    public static void startActivity(Activity activity, ArrayList<String> roomIds, int position, boolean isCreate) {
        IntentWrap.launchVoiceRoom(activity, roomIds, position, isCreate);
    }


    @Override
    protected void initRoom() {
        isCreate = getIntent().getBooleanExtra(IntentWrap.KEY_IS_CREATE, false);
    }

    @Override
    public Fragment getFragment(String roomId) {
        return VoiceRoomFragment.getInstance(roomId, isCreate);
    }


    @Override
    protected RoomType getRoomType() {
        return RoomType.VOICE_ROOM;
    }


}
