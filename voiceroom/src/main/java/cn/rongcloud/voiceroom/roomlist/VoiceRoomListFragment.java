package cn.rongcloud.voiceroom.roomlist;

import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;

import java.util.ArrayList;

import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.ui.roomlist.AbsRoomListFragment;
import cn.rongcloud.voiceroom.room.VoiceRoomActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class VoiceRoomListFragment extends AbsRoomListFragment {

    public static Fragment getInstance() {
        return new VoiceRoomListFragment();
    }

    @Override
    public void launchRoomActivity(ArrayList<String> roomIds, int position, boolean isCreate) {
        VoiceRoomActivity.startActivity(getActivity(), roomIds, position, isCreate);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.VOICE_ROOM;
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }

    @Override
    public void initListener() {

    }

}
