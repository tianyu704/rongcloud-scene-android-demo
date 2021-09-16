package cn.rongcloud.radioroom.ui.roomlist;

import androidx.fragment.app.Fragment;

import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.roomlist.RoomListFragment;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomListFragment extends RoomListFragment {

    public static Fragment getInstance() {
        return new RadioRoomListFragment();
    }

    @Override
    public void clickItem(VoiceRoomBean item, int position) {

    }

    @Override
    public RoomType getRoomType() {
        return RoomType.VOICE_ROOM;
    }
}
