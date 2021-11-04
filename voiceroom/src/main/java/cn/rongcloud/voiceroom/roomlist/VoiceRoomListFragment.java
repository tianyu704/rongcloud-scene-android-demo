package cn.rongcloud.voiceroom.roomlist;

import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.common.ui.dialog.ConfirmDialog;
import cn.rong.combusis.common.ui.dialog.InputPasswordDialog;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.roomlist.AbsRoomListFragment;
import cn.rong.combusis.widget.miniroom.MiniRoomManager;
import cn.rongcloud.voiceroom.room.VoiceRoomActivity;
import io.rong.imkit.picture.tools.ToastUtils;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class VoiceRoomListFragment extends AbsRoomListFragment {

    private ConfirmDialog confirmDialog;

    public static Fragment getInstance() {
        return new VoiceRoomListFragment();
    }

    private InputPasswordDialog inputPasswordDialog;

    @Override
    public void clickItem(VoiceRoomBean item, int position, boolean isCreate, List<VoiceRoomBean> voiceRoomBeans) {
        MiniRoomManager.getInstance().finish(item.getRoomId(), () -> {
            if (TextUtils.equals(item.getUserId(), AccountStore.INSTANCE.getUserId())) {
                ArrayList list = new ArrayList();
                list.add(item.getRoomId());
                VoiceRoomActivity.startActivity(getActivity(), list, 0, isCreate);
            } else if (item.isPrivate()) {
                inputPasswordDialog = new InputPasswordDialog(requireContext(), false, () -> null, s -> {
                    if (TextUtils.isEmpty(s)) {
                        return null;
                    }
                    if (s.length() < 4) {
                        ToastUtils.s(requireContext(), requireContext().getString(cn.rong.combusis.R.string.text_please_input_four_number));
                        return null;
                    }
                    if (TextUtils.equals(s, item.getPassword())) {
                        inputPasswordDialog.dismiss();
                        ArrayList list = new ArrayList();
                        list.add(item.getRoomId());
                        VoiceRoomActivity.startActivity(getActivity(), list, 0, false);
                    } else {
                        showToast("密码错误");
                    }
                    return null;
                });
                inputPasswordDialog.show();
            } else {
                ArrayList<String> list = new ArrayList<>();
                for (VoiceRoomBean voiceRoomBean : voiceRoomBeans) {
                    if (!voiceRoomBean.getCreateUserId().equals(AccountStore.INSTANCE.getUserId()) && !voiceRoomBean.isPrivate()) {
                        //过滤掉上锁的房间和自己创建的房间
                        list.add(voiceRoomBean.getRoomId());
                    }
                }
                int p = list.indexOf(item.getRoomId());
                if (p < 0) p = 0;
                VoiceRoomActivity.startActivity(getActivity(), list, p, false);
            }
        });
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
