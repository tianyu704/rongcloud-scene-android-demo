package cn.rongcloud.radioroom.ui.roomlist;

import com.basis.ui.BaseFragment;
import com.kit.wapper.IResultBack;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
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

        VoiceRoomProvider.provider().getAsyn("", new IResultBack<VoiceRoomBean>() {
            @Override
            public void onResult(VoiceRoomBean voiceRoomBean) {

            }
        });
    }

}
