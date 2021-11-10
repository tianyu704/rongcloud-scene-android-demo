package cn.rongcloud.radio.ui.room;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import cn.rong.combusis.intent.IntentWrap;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.ui.room.AbsRoomActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomActivity extends AbsRoomActivity {

    public static void startActivity(Activity activity, ArrayList<String> roomIds, int position) {
        IntentWrap.launchRadioRoom(activity, roomIds, position);
    }

    @Override
    protected void initRoom() {
    }

    @Override
    public Fragment getFragment(String roomId) {
        return RadioRoomFragment.getInstance(roomId);
    }

    @Override
    protected RoomType getRoomType() {
        return RoomType.RADIO_ROOM;
    }

}
