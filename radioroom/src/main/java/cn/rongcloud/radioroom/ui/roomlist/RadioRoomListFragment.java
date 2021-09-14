package cn.rongcloud.radioroom.ui.roomlist;

import com.basis.ui.BaseFragment;

import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomListFragment extends BaseFragment {
    @Override
    public int setLayoutId() {
        return 0;
    }

    @Override
    public void init() {
        VoiceRoomProvider.provider().loadPage(1, voiceRoomBeans -> {
        });
    }

}
