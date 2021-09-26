package cn.rongcloud.voiceroom.roomlist;

import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;

import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.roomlist.AbsRoomListFragment;
import cn.rongcloud.voiceroom.room.NewVoiceRoomActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class VoiceRoomListFragment extends AbsRoomListFragment {

    public static Fragment getInstance() {
        return new VoiceRoomListFragment();
    }

    @Override
    public void clickItem(VoiceRoomBean item, int position) {
        NewVoiceRoomActivity.startActivity(getActivity(), getRoomIdList(), position);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.VOICE_ROOM;
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }
}
