package cn.rongcloud.radio.ui.room;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

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
    protected int getCurrentItem() {
        if (getIntent().hasExtra(IntentWrap.KEY_ROOM_POSITION)) {
            return getIntent().getIntExtra(IntentWrap.KEY_ROOM_POSITION, 0);
        }
        return 0;
    }

    @Override
    public Fragment getFragment(String roomId) {
        return RadioRoomFragment.getInstance(roomId);
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
        return RoomType.RADIO_ROOM;
    }

}
