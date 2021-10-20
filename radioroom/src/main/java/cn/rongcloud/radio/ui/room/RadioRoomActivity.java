package cn.rongcloud.radio.ui.room;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.ui.room.AbsRoomActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomActivity extends AbsRoomActivity {
    private static final String KEY_ROOM_IDS = "KEY_ROOM_IDS";
    private static final String KEY_ROOM_POSITION = "KEY_ROOM_POSITION";

    public static void startActivity(Activity activity, ArrayList<String> roomIds, int position) {
        Intent intent = new Intent(activity, RadioRoomActivity.class);
        intent.putStringArrayListExtra(KEY_ROOM_IDS, roomIds);
        intent.putExtra(KEY_ROOM_POSITION, position);
        activity.startActivity(intent);
    }

    @Override
    protected void initRoom() {
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
        return RadioRoomFragment.getInstance(roomId);
    }

    @Override
    public List<String> loadData() {
        if (getIntent().hasExtra(KEY_ROOM_IDS)) {
            ArrayList<String> ids = getIntent().getStringArrayListExtra(KEY_ROOM_IDS);
            return ids;
        }
        return null;
    }

}
