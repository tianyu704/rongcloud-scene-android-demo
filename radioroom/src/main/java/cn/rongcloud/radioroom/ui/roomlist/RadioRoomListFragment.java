package cn.rongcloud.radioroom.ui.roomlist;

import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;

import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.roomlist.AbsRoomListFragment;
import cn.rongcloud.radioroom.ui.room.RadioRoomActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomListFragment extends AbsRoomListFragment {

    public static Fragment getInstance() {
        return new RadioRoomListFragment();
    }

    @Override
    public void clickItem(VoiceRoomBean item, int position) {
        if (TextUtils.equals(item.getUserId(), AccountStore.INSTANCE.getUserId())) {
            ArrayList list = new ArrayList();
            list.add(item.getRoomId());
            RadioRoomActivity.startActivity(getActivity(), list, 0);
        } else {
            RadioRoomActivity.startActivity(getActivity(), getRoomIdList(), position);
        }
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.RADIO_ROOM;
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }
}
