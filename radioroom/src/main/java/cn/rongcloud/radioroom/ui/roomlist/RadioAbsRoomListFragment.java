package cn.rongcloud.radioroom.ui.roomlist;

import androidx.fragment.app.Fragment;

import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.roomlist.AbsRoomListFragment;
import cn.rongcloud.radioroom.ui.room.RadioRoomActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioAbsRoomListFragment extends AbsRoomListFragment {

    public static Fragment getInstance() {
        return new RadioAbsRoomListFragment();
    }

    @Override
    public void clickItem(VoiceRoomBean item, int position) {
        RadioRoomActivity.startActivity(getActivity(), getRoomIdList(), position);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.RADIO_ROOM;
    }
}
