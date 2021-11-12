package cn.rongcloud.liveroom.room;

import static cn.rong.combusis.intent.IntentWrap.KEY_IS_CREATE;

import androidx.fragment.app.Fragment;

import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.ui.room.AbsRoomActivity;

/**
 * @author lihao1
 * @date 2021/9/14
 */
public class LiveRoomActivity extends AbsRoomActivity {

    @Override
    protected void initRoom() {
    }

    @Override
    public Fragment getFragment(String roomId) {
        boolean isCreate = getIntent().getBooleanExtra(KEY_IS_CREATE, false);
        if (isCreate) {
            //如果当时是创建房间的情况
            return CreatLiveRoomFragment.getInstance();
        } else {
            //观众端和主播房还需要细分
            return null;
        }
    }

    @Override
    protected RoomType getRoomType() {
        return RoomType.LIVE_ROOM;
    }

}
