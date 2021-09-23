package cn.rongcloud.radioroom.ui.room;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.rongcloud.common.utils.ImageLoaderUtil;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.radioroom.R;

/**
 * @author gyn
 * @date 2021/9/17
 */
public class RadioRoomFragment extends AbsRoomFragment {
    public static String KEY_ROOM_ID = "KEY_ROOM_ID";
    private String mRoomId;
    private VoiceRoomBean mVoiceRoomBean;
    private RoomTitleBar mRoomTitleBar;
    private ImageView mBackgroundImageView;

    public static Fragment getInstance(String roomId) {
        Fragment fragment = new RadioRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ROOM_ID, roomId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_radio_room;
    }

    @Override
    public void init() {
        mRoomTitleBar = getView(R.id.room_title_bar);
        mRoomTitleBar.setOnMenuClickListener(v -> {

        });
        mBackgroundImageView = getView(R.id.iv_background);
        if (getArguments() != null && getArguments().getString(KEY_ROOM_ID) != null) {
            mRoomId = getArguments().getString(KEY_ROOM_ID);
            loadRoomDataFromCache();
        }
    }

    private void loadRoomDataFromCache() {
        VoiceRoomProvider.provider().getAsyn(mRoomId, voiceRoomBean -> {
            mVoiceRoomBean = voiceRoomBean;
            mRoomTitleBar.setData(mVoiceRoomBean.getRoomName(), mVoiceRoomBean.getId());
            ImageLoaderUtil.INSTANCE.loadImage(requireContext(), mBackgroundImageView, mVoiceRoomBean.getBackgroundUrl(), R.color.black);
        });
    }

    @Override
    public void joinRoom() {

    }

    @Override
    public void destroyRoom() {

    }
}
